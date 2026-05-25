package com.Nguyen.blogplatform.service.export;

import com.Nguyen.blogplatform.payload.response.analytics.ReportRequestDTO;

public interface AnalyticsExportService {

    byte[] generatePdfReport(ReportRequestDTO request);

    byte[] generateExcelReport(ReportRequestDTO request);
}