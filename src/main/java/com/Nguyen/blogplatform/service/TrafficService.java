package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.model.Traffic;
import com.Nguyen.blogplatform.model.Traffic.PeriodType;
import com.Nguyen.blogplatform.repository.TrafficRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TrafficService {
    private static final Logger log = LoggerFactory.getLogger(TrafficService.class);

    private final TrafficRepository trafficRepository;

    public TrafficService(TrafficRepository trafficRepository) {
        this.trafficRepository = trafficRepository;
    }

    @Transactional
    public void recordHit(ZoneId zone) {
        LocalDate now = LocalDate.now(zone);
        // Upsert day
        upsert(PeriodType.DAY, now);
        // Upsert month (use first day of month as key)
        LocalDate monthKey = now.withDayOfMonth(1);
        upsert(PeriodType.MONTH, monthKey);
        // Upsert year (use first day of year as key)
        LocalDate yearKey = now.withDayOfYear(1);
        upsert(PeriodType.YEAR, yearKey);
    }

    private void upsert(PeriodType type, LocalDate dateKey) {
        int updated = trafficRepository.incrementCounter(type, dateKey, 1L);
        if (updated == 0) {
            // Create row then increment to handle race conditions
            Traffic entity = new Traffic(dateKey, type);
            entity.setAccessCount(1);
            try {
                trafficRepository.save(entity);
            } catch (Exception ex) {
                // Handle unique constraint race; retry increment
                log.debug("Traffic upsert race for {} {}: {}", type, dateKey, ex.getMessage());
                trafficRepository.incrementCounter(type, dateKey, 1L);
            }
        }
    }

    public record TrafficPoint(String period, long count,String zone ,String type) {}

    @Cacheable(value = "trafficStats", key = "#type + ':' + #start + ':' + #end")
    @Transactional(readOnly = true)
    public List<TrafficPoint> getStats(PeriodType type, LocalDate start, LocalDate end) {
        List<Traffic> rows = trafficRepository.findAllByPeriodTypeAndPeriodDateBetweenOrderByPeriodDateAsc(type, start, end);
        DateTimeFormatter fmt = switch (type) {
            case DAY -> DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd
            case MONTH -> DateTimeFormatter.ofPattern("yyyy-MM");
            case YEAR -> DateTimeFormatter.ofPattern("yyyy");
        };
        List<TrafficPoint> points = new ArrayList<>();
        for (Traffic t : rows) {
            String label = switch (type) {
                case DAY -> t.getPeriodDate().format(fmt);
                case MONTH -> t.getPeriodDate().format(fmt);
                case YEAR -> t.getPeriodDate().format(fmt);
            };
            points.add(new TrafficPoint(label,
                    t.getAccessCount(),
                    t.getZoneId(),
                    type.name().toLowerCase(Locale.ROOT)));
        }
        return points;
    }
}
