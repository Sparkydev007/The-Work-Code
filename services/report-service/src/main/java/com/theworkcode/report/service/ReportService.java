package com.theworkcode.report.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.report.entity.ReportEntity;
import com.theworkcode.report.pdf.PdfReportGenerator;
import com.theworkcode.report.repository.ReportRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository repository;
    private final PdfReportGenerator pdfGenerator;

    @Transactional
    public Map<String, Object> generate(Map<String, Object> request, String generatedBy) {
        String verificationId = String.valueOf(request.getOrDefault("verificationId", ""));
        String code = "RPT-" + Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()), 36)
                .toUpperCase().substring(0, 6);

        ReportEntity e = new ReportEntity();
        e.setReportCode(code);
        e.setVerificationId(verificationId);
        e.setVerificationCode(str(request, "verificationCode"));
        e.setReportType(str(request, "verificationType", "EMPLOYMENT_AND_INCOME"));
        e.setApplicantName(str(request, "applicantName"));
        e.setEmployerName(str(request, "employerName"));
        e.setResult(str(request, "result"));
        if (request.get("confidence") instanceof Number n) {
            e.setConfidence(java.math.BigDecimal.valueOf(n.doubleValue()));
        }
        e.setGeneratedBy(generatedBy);
        e.setGeneratedAt(OffsetDateTime.now());
        e.setAccessCount(0);

        Map<String, Object> pdfModel = new HashMap<>(request);
        pdfModel.put("reportCode", code);
        pdfModel.put("generatedAt", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(e.getGeneratedAt()));
        pdfModel.put("generatedBy", generatedBy);
        e.setContent(writeJson(pdfModel));

        byte[] pdf = pdfGenerator.generate(pdfModel);
        e.setPdfBytes(pdf);
        e = repository.save(e);
        log.info("report_generated code={} bytes={} verification={}", code, pdf.length, verificationId);

        return Map.of(
                "reportCode", code,
                "verificationId", verificationId,
                "reportType", e.getReportType(),
                "sizeBytes", pdf.length);
    }

    @Transactional(readOnly = true)
    public Page<ReportEntity> list(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(200, Math.max(1, size)));
        return repository.findAllByOrderByGeneratedAtDesc(pageable);
    }

    @Transactional
    public ReportEntity get(String reportCode) {
        ReportEntity e = repository.findByReportCode(reportCode)
                .orElseThrow(() -> new ApiException(ErrorCode.REPORT_NOT_FOUND,
                        "Report '" + reportCode + "' was not found."));
        e.setAccessCount(e.getAccessCount() + 1);
        return repository.save(e);
    }

    @Transactional
    public byte[] download(String reportCode) {
        return get(reportCode).getPdfBytes();
    }

    @Transactional
    public Map<String, Object> regenerate(String reportCode, String generatedBy) {
        ReportEntity e = repository.findByReportCode(reportCode)
                .orElseThrow(() -> new ApiException(ErrorCode.REPORT_NOT_FOUND,
                        "Report '" + reportCode + "' was not found."));
        Map<String, Object> model = readJson(e.getContent());
        model.put("reportCode", e.getReportCode());
        model.put("generatedAt", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()));
        model.put("generatedBy", generatedBy);
        byte[] pdf = pdfGenerator.generate(model);
        e.setPdfBytes(pdf);
        e.setGeneratedAt(OffsetDateTime.now());
        e.setGeneratedBy(generatedBy);
        repository.save(e);
        log.info("report_regenerated code={}", reportCode);
        return Map.of("reportCode", reportCode, "sizeBytes", pdf.length, "regenerated", true);
    }

    private String str(Map<String, Object> map, String key) {
        return str(map, key, null);
    }

    private String str(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v == null ? def : String.valueOf(v);
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> readJson(String json) {
        if (json == null) {
            return new HashMap<>();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
