package com.Nguyen.blogplatform.domain.analytics.application.export.impl;

import com.Nguyen.blogplatform.domain.analytics.application.AnalyticsService;
import com.Nguyen.blogplatform.domain.analytics.dto.DashboardSummaryDTO;
import com.Nguyen.blogplatform.domain.analytics.dto.ReportRequestDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalyticsExportServiceTest {
    @Test void exportsValidPdfAndExcelReports() throws Exception {
        var analytics = mock(AnalyticsService.class);
        when(analytics.getDashboardSummary()).thenReturn(new DashboardSummaryDTO(42, 2, 3, 4));
        var service = new AnalyticsExportServiceImpl(analytics);
        var request = new ReportRequestDTO(ReportRequestDTO.ReportType.YEAR, 2026, null, null,
                ReportRequestDTO.ExportFormat.PDF);
        byte[] pdf = service.generatePdfReport(request);
        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
        byte[] excel = service.generateExcelReport(request);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            assertEquals(4, workbook.getNumberOfSheets());
            assertNotNull(workbook.getSheet("Dashboard Summary"));
        }
    }
}
