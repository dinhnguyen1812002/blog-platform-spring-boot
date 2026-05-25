package com.Nguyen.blogplatform.service.export.impl;

import com.Nguyen.blogplatform.payload.response.analytics.*;
import com.Nguyen.blogplatform.service.analytics.AnalyticsService;
import com.Nguyen.blogplatform.service.export.AnalyticsExportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsExportServiceImpl implements AnalyticsExportService {

    private final AnalyticsService analyticsService;

    @Override
    public byte[] generatePdfReport(ReportRequestDTO request) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(new PdfWriter(baos));
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Analytics Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(20)
                    .setFontColor(ColorConstants.BLUE));
            document.add(new Paragraph("Generated: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10));
            document.add(new Paragraph("\n"));

            addDashboardSummaryToPdf(document, request);
            addMonthlyStatsToPdf(document, request);
            addTopPostsToPdf(document, request);
            addTopAuthorsToPdf(document, request);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @Override
    public byte[] generateExcelReport(ReportRequestDTO request) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet summarySheet = workbook.createSheet("Dashboard Summary");
            Sheet monthlySheet = workbook.createSheet("Monthly Stats");
            Sheet postsSheet = workbook.createSheet("Top Posts");
            Sheet authorsSheet = workbook.createSheet("Top Authors");

            createSummarySheet(summarySheet, request);
            createMonthlySheet(monthlySheet, request);
            createPostsSheet(postsSheet, request);
            createAuthorsSheet(authorsSheet, request);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private void addDashboardSummaryToPdf(Document document, ReportRequestDTO request) {
        document.add(new Paragraph("Dashboard Summary").setBold().setFontSize(14));
        DashboardSummaryDTO summary = analyticsService.getDashboardSummary();

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        table.addCell("Total Views");
        table.addCell(String.valueOf(summary.totalViews()));
        table.addCell("Active Users");
        table.addCell(String.valueOf(summary.activeUsers()));
        table.addCell("New Posts (This Month)");
        table.addCell(String.valueOf(summary.newPosts()));
        table.addCell("Total Likes");
        table.addCell(String.valueOf(summary.totalLikes()));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addMonthlyStatsToPdf(Document document, ReportRequestDTO request) {
        int year = request.year();
        document.add(new Paragraph("Monthly Statistics - " + year).setBold().setFontSize(14));

        List<MonthlyGrowthDTO> monthlyGrowth = analyticsService.getMonthlyGrowth(year);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1}))
                .useAllAvailableWidth();

        table.addHeaderCell("Month");
        table.addHeaderCell("New Users");
        table.addHeaderCell("New Posts");
        table.addHeaderCell("Total Views");
        table.addHeaderCell("Growth Rate (%)");

        for (MonthlyGrowthDTO dto : monthlyGrowth) {
            table.addCell(String.valueOf(dto.month()));
            table.addCell(String.valueOf(dto.newUsers()));
            table.addCell(String.valueOf(dto.newPosts()));
            table.addCell(String.valueOf(dto.totalViews()));
            table.addCell(String.format("%.2f", dto.growthRate()));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTopPostsToPdf(Document document, ReportRequestDTO request) {
        document.add(new Paragraph("Top Posts").setBold().setFontSize(14));

        List<TopPostDTO> topPosts = analyticsService.getMostViewedPosts(10);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 1, 1}))
                .useAllAvailableWidth();

        table.addHeaderCell("ID");
        table.addHeaderCell("Title");
        table.addHeaderCell("Author");
        table.addHeaderCell("Views");
        table.addHeaderCell("Likes");

        for (TopPostDTO post : topPosts) {
            table.addCell(post.postId());
            table.addCell(post.title());
            table.addCell(post.authorName());
            table.addCell(String.valueOf(post.viewCount()));
            table.addCell(String.valueOf(post.likeCount()));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addTopAuthorsToPdf(Document document, ReportRequestDTO request) {
        document.add(new Paragraph("Top Authors").setBold().setFontSize(14));

        List<TopAuthorDTO> topAuthors = analyticsService.getTopAuthors(10);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1}))
                .useAllAvailableWidth();

        table.addHeaderCell("Author ID");
        table.addHeaderCell("Author Name");
        table.addHeaderCell("Total Views");
        table.addHeaderCell("Total Likes");
        table.addHeaderCell("Post Count");

        for (TopAuthorDTO author : topAuthors) {
            table.addCell(author.authorId());
            table.addCell(author.authorName());
            table.addCell(String.valueOf(author.totalViews()));
            table.addCell(String.valueOf(author.totalLikes()));
            table.addCell(String.valueOf(author.postCount()));
        }

        document.add(table);
    }

    private void createSummarySheet(Sheet sheet, ReportRequestDTO request) {
        DashboardSummaryDTO summary = analyticsService.getDashboardSummary();

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Metric");
        headerRow.createCell(1).setCellValue("Value");

        int rowNum = 1;
        createDataRow(sheet, rowNum++, "Total Views", String.valueOf(summary.totalViews()));
        createDataRow(sheet, rowNum++, "Active Users", String.valueOf(summary.activeUsers()));
        createDataRow(sheet, rowNum++, "New Posts (This Month)", String.valueOf(summary.newPosts()));
        createDataRow(sheet, rowNum++, "Total Likes", String.valueOf(summary.totalLikes()));

        autoSizeColumns(sheet, 2);
    }

    private void createMonthlySheet(Sheet sheet, ReportRequestDTO request) {
        int year = request.year();
        List<MonthlyGrowthDTO> monthlyGrowth = analyticsService.getMonthlyGrowth(year);

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Month");
        headerRow.createCell(1).setCellValue("New Users");
        headerRow.createCell(2).setCellValue("New Posts");
        headerRow.createCell(3).setCellValue("Total Views");
        headerRow.createCell(4).setCellValue("Growth Rate (%)");

        int rowNum = 1;
        for (MonthlyGrowthDTO dto : monthlyGrowth) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.month());
            row.createCell(1).setCellValue(dto.newUsers());
            row.createCell(2).setCellValue(dto.newPosts());
            row.createCell(3).setCellValue(dto.totalViews());
            row.createCell(4).setCellValue(dto.growthRate());
        }

        autoSizeColumns(sheet, 5);
    }

    private void createPostsSheet(Sheet sheet, ReportRequestDTO request) {
        List<TopPostDTO> topPosts = analyticsService.getMostViewedPosts(50);

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Post ID");
        headerRow.createCell(1).setCellValue("Title");
        headerRow.createCell(2).setCellValue("Author");
        headerRow.createCell(3).setCellValue("Views");
        headerRow.createCell(4).setCellValue("Likes");

        int rowNum = 1;
        for (TopPostDTO post : topPosts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(post.postId());
            row.createCell(1).setCellValue(post.title());
            row.createCell(2).setCellValue(post.authorName());
            row.createCell(3).setCellValue(post.viewCount());
            row.createCell(4).setCellValue(post.likeCount());
        }

        autoSizeColumns(sheet, 5);
    }

    private void createAuthorsSheet(Sheet sheet, ReportRequestDTO request) {
        List<TopAuthorDTO> topAuthors = analyticsService.getTopAuthors(50);

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Author ID");
        headerRow.createCell(1).setCellValue("Author Name");
        headerRow.createCell(2).setCellValue("Total Views");
        headerRow.createCell(3).setCellValue("Total Likes");
        headerRow.createCell(4).setCellValue("Post Count");

        int rowNum = 1;
        for (TopAuthorDTO author : topAuthors) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(author.authorId());
            row.createCell(1).setCellValue(author.authorName());
            row.createCell(2).setCellValue(author.totalViews());
            row.createCell(3).setCellValue(author.totalLikes());
            row.createCell(4).setCellValue(author.postCount());
        }

        autoSizeColumns(sheet, 5);
    }

    private void createDataRow(Sheet sheet, int rowNum, String metric, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(metric);
        row.createCell(1).setCellValue(value);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}