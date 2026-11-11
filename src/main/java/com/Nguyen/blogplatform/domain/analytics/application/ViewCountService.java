package com.Nguyen.blogplatform.domain.analytics.application;



import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;

import com.Nguyen.blogplatform.domain.series.infrastructure.repository.SeriesRepository;
import glide.api.GlideClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.UUID;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Generic service to handle view counts for different entities (Posts, Series, etc.)
 * Uses Valkey (Redis) for IP-based uniqueness (24h) and buffering counts for performance.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ViewCountService {

    private final GlideClient glideClient;
    private final PostRepository postRepository;
    private final SeriesRepository seriesRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    private static final String UNIQUE_VIEW_KEY_PREFIX = "view:unique:";
    private static final String BUFFER_VIEW_KEY_PREFIX = "view:buffer:";
    private static final String TO_SYNC_SET_KEY = "view:to_sync:"; // e.g., view:to_sync:post

    public static final String ENTITY_TYPE_POST = "post";
    public static final String ENTITY_TYPE_SERIES = "series";

    /**
     * Records a view for an entity.
     * 
     * @param entityType The type of entity (e.g., "post", "series")
     * @param entityId The ID of the entity
     */
    public void recordView(String entityType, String entityId) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.debug("No request attributes found, skipping view recording for {}: {}", entityType, entityId);
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteAddr();
        String uniqueKey = UNIQUE_VIEW_KEY_PREFIX + entityType + ":" + entityId + ":" + ip;

        try {
            String bufferKey = BUFFER_VIEW_KEY_PREFIX + entityType + ":" + entityId;
            String script = "if redis.call('SET', KEYS[1], '1', 'NX', 'EX', 86400) then " +
                    "redis.call('INCR', KEYS[2]); redis.call('SADD', KEYS[3], ARGV[1]); return 1 end; return 0";
            glideClient.customCommand(new String[]{"EVAL", script, "3", uniqueKey, bufferKey,
                    TO_SYNC_SET_KEY + entityType, entityId}).get();
        } catch (Exception e) {
            log.error("Error recording view for {}: {} in Valkey", entityType, entityId, e);
        }
    }

    /**
     * Scheduled task to sync view counts from Valkey to the database.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000)
    public void syncViewsToDatabase() {
        log.info("Starting view count synchronization from Valkey to Database...");
        syncType(ENTITY_TYPE_POST);
        syncType(ENTITY_TYPE_SERIES);
    }

    private void syncType(String entityType) {
        try {
            Object response = glideClient.customCommand(new String[]{"SMEMBERS", TO_SYNC_SET_KEY + entityType}).get();
            if (response instanceof Object[] ids && ids.length > 0) {
                log.info("Found {} {}s with pending view counts", ids.length, entityType);
                for (Object idObj : ids) {
                    syncEntityView(entityType, idObj.toString());
                }
            }
        } catch (Exception e) {
            log.error("Error during view count synchronization for {}", entityType, e);
        }
    }

    private void syncEntityView(String entityType, String id) {
        String bufferKey = BUFFER_VIEW_KEY_PREFIX + entityType + ":" + id;
        String pendingKey = "view:pending:" + entityType + ":" + id;
        String setKey = TO_SYNC_SET_KEY + entityType;
        try {
            String claim = "local p=redis.call('GET',KEYS[2]); if p then return p end; " +
                    "local n=tonumber(redis.call('GET',KEYS[1]) or '0'); if n<=0 then return nil end; " +
                    "p=ARGV[1]..':'..n; redis.call('SET',KEYS[2],p); redis.call('SET',KEYS[1],'0'); return p";
            Object pending = glideClient.customCommand(new String[]{"EVAL", claim, "2", bufferKey,
                    pendingKey, UUID.randomUUID().toString()}).get();
            if (pending == null) {
                String cleanup = "if not redis.call('GET',KEYS[2]) and tonumber(redis.call('GET',KEYS[1]) or '0')==0 then redis.call('SREM',KEYS[3],ARGV[1]) end";
                glideClient.customCommand(new String[]{"EVAL", cleanup, "3", bufferKey, pendingKey, setKey, id}).get();
                return;
            }
            String batch = pending.toString();
            int separator = batch.lastIndexOf(':');
            String batchId = batch.substring(0, separator);
            long increment = Long.parseLong(batch.substring(separator + 1));
            transactionTemplate.executeWithoutResult(status -> {
                int inserted = jdbcTemplate.update("INSERT IGNORE INTO view_sync_batch (batch_id) VALUES (?)", batchId);
                if (inserted == 1) {
                    if (ENTITY_TYPE_POST.equals(entityType)) postRepository.updateViewCount(id, increment);
                    else if (ENTITY_TYPE_SERIES.equals(entityType)) seriesRepository.updateViewCount(id, increment);
                    else throw new IllegalArgumentException("Unknown view entity type");
                }
            });
            String ack = "if redis.call('GET',KEYS[2])==ARGV[1] then redis.call('DEL',KEYS[2]); " +
                    "if tonumber(redis.call('GET',KEYS[1]) or '0')==0 then redis.call('SREM',KEYS[3],ARGV[2]) end end";
            glideClient.customCommand(new String[]{"EVAL", ack, "3", bufferKey, pendingKey, setKey, batch, id}).get();
        } catch (Exception e) {
            log.error("Error syncing views for {}: {}; pending batch will be retried", entityType, id, e);
        }
    }
}
