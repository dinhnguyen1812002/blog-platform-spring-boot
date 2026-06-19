package com.Nguyen.blogplatform.domain.analytics.application.export;




import com.Nguyen.blogplatform.domain.analytics.dto.ReportRequestDTO;
public interface AnalyticsExportService {

    byte[] generatePdfReport(ReportRequestDTO request);

    byte[] generateExcelReport(ReportRequestDTO request);
}