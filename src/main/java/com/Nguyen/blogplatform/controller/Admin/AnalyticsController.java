package com.Nguyen.blogplatform.controller.Admin;

import com.Nguyen.blogplatform.payload.response.AnalyticsResponse;
import com.Nguyen.blogplatform.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;


    @GetMapping
    public AnalyticsResponse getAnalytics() {
        return analyticsService.analytics();
    }

}


