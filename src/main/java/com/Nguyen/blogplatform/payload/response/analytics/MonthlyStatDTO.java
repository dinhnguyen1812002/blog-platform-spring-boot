package com.Nguyen.blogplatform.payload.response.analytics;

public record MonthlyStatDTO(
    int year,
    int month,
    long count
) {}