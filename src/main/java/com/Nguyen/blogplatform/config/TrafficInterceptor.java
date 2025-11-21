package com.Nguyen.blogplatform.config;

import com.Nguyen.blogplatform.service.TrafficService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.ZoneId;

public class TrafficInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(TrafficInterceptor.class);

    private final TrafficService trafficService;
    private final ZoneId zoneId;

    public TrafficInterceptor(TrafficService trafficService, ZoneId zoneId) {
        this.trafficService = trafficService;
        this.zoneId = zoneId;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String path = request.getRequestURI();
            if (shouldCount(path)) {
                trafficService.recordHit(zoneId);
            }
        } catch (Exception e) {
            log.warn("Traffic counting failed: {}", e.getMessage());
        }
        return true;
    }

    private boolean shouldCount(String path) {
        return !(path.startsWith("/actuator") || path.startsWith("/ws") || path.startsWith("/images") || path.startsWith("/videos") || path.startsWith("/static") || path.startsWith("/css") || path.startsWith("/js"));
    }
}
