package com.Nguyen.blogplatform.config;

import com.Nguyen.blogplatform.service.TrafficService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.ZoneId;

@Configuration
public class TrafficWebMvcConfig implements WebMvcConfigurer {

    private final TrafficService trafficService;

    public TrafficWebMvcConfig(TrafficService trafficService) {
        this.trafficService = trafficService;
    }

    @Bean
    public ZoneId appZoneId() {
        return ZoneId.systemDefault();
    }

    @Bean
    public TrafficInterceptor trafficInterceptor(ZoneId appZoneId) {
        return new TrafficInterceptor(trafficService, appZoneId);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trafficInterceptor(appZoneId()))
                .addPathPatterns("/**");
    }
}
