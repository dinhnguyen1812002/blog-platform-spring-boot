package com.Nguyen.blogplatform.controller.traffic;

import com.Nguyen.blogplatform.model.Traffic.PeriodType;
import com.Nguyen.blogplatform.service.TrafficService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/traffic")
public class TrafficController {
    private static final Logger log = LoggerFactory.getLogger(TrafficController.class);

    private final TrafficService trafficService;
    private final ZoneId zoneId;

    public TrafficController(TrafficService trafficService, ZoneId zoneId) {
        this.trafficService = trafficService;
        this.zoneId = zoneId;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        PeriodType type = parsePeriod(period);
        LocalDate now = LocalDate.now(zoneId);
        if (start == null || end == null) {
            switch (type) {
                case DAY -> {
                    // last 30 days including today
                    end = now;
                    start = now.minusDays(29);
                }
                case MONTH -> {
                    end = now.withDayOfMonth(1);
                    start = end.minusMonths(11);
                }
                case YEAR -> {
                    end = now.withDayOfYear(1);
                    start = end.minusYears(4);
                }
            }
        }
        List<TrafficService.TrafficPoint> data = trafficService.getStats(type, start, end);
        Map<String, Object> body = new HashMap<>();
        body.put("periodType", type.name().toLowerCase(Locale.ROOT));
        body.put("access_count",  data.size() );
        body.put("start", start.toString());
        body.put("end", end.toString());
        body.put("points", data);
        return ResponseEntity.ok(body);
    }

    private PeriodType parsePeriod(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "day", "daily", "d" -> PeriodType.DAY;
            case "month", "monthly", "m" -> PeriodType.MONTH;
            case "year", "yearly", "y" -> PeriodType.YEAR;
            default -> throw new IllegalArgumentException("Unsupported period: " + value);
        };
    }
}
