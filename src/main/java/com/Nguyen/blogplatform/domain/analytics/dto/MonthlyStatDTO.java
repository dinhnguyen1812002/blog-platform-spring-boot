package com.Nguyen.blogplatform.domain.analytics.dto;



public record MonthlyStatDTO(
    int year,
    int month,
    long count
) {}