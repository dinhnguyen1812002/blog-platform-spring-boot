package com.Nguyen.blogplatform.service.analytics;

import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.SeriesRepository;
import glide.api.GlideClient;
import glide.api.models.commands.SetOptions;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
            // Try to set the unique key with NX (only if not exists) and EX (expiry 24h)
            String result = glideClient.set(uniqueKey, "1", SetOptions.builder()
                    .conditionalSet(SetOptions.ConditionalSet.ONLY_IF_DOES_NOT_EXIST)
                    .expiry(SetOptions.Expiry.Seconds(86400L)) // 24 hours
                    .build()).get();

            if (result != null) {
                // Unique view detected
                String bufferKey = BUFFER_VIEW_KEY_PREFIX + entityType + ":" + entityId;
                glideClient.incr(bufferKey).get();
                // Add to the set of entities that need syncing
                glideClient.customCommand(new String[]{"SADD", TO_SYNC_SET_KEY + entityType, entityId}).get();
                log.debug("Recorded unique view for {}: {} from IP: {}", entityType, entityId, ip);
            }
        } catch (Exception e) {
            log.error("Error recording view for {}: {} in Valkey", entityType, entityId, e);
        }
    }

    /**
     * Scheduled task to sync view counts from Valkey to the database.
     * Runs every 10 minutes.
     */
    @Scheduled(fixedRate = 600000)
    @Transactional
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
        try {
            // Atomically get the current count and reset it to "0"
            Object countObj = glideClient.customCommand(new String[]{"GETSET", bufferKey, "0"}).get();
            if (countObj != null) {
                long increment = Long.parseLong(countObj.toString());
                if (increment > 0) {
                    if (ENTITY_TYPE_POST.equals(entityType)) {
                        postRepository.updateViewCount(id, increment);
                    } else if (ENTITY_TYPE_SERIES.equals(entityType)) {
                        seriesRepository.updateViewCount(id, increment);
                    }
                    log.debug("Synced {} views for {}: {}", increment, entityType, id);
                }
            }
            // Remove from sync set after successful sync
            glideClient.customCommand(new String[]{"SREM", TO_SYNC_SET_KEY + entityType, id}).get();
        } catch (Exception e) {
            log.error("Error syncing views for {}: {}", entityType, id, e);
        }
    }
}
