package com.theworkcode.batch.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.theworkcode.common.api.ApiException;
import com.theworkcode.common.api.ErrorCode;
import com.theworkcode.batch.entity.BatchItemEntity;
import com.theworkcode.batch.entity.BatchJobEntity;
import com.theworkcode.batch.repository.BatchItemRepository;
import com.theworkcode.batch.repository.BatchJobRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.data.domain.Sort;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Batch verification pipeline:
 * Upload CSV -> Validate rows -> Preview -> Submit -> Process (async demo) ->
 * Results -> Download result CSV. Expected columns:
 * employee_name, employee_id, employer, verification_type.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchJobRepository jobRepository;
    private final BatchItemRepository itemRepository;

    /** Processed row outcomes keyed by batchId, used by the simulated processor. */
    private final Map<UUID, List<BatchItemEntity>> stagedRows = new ConcurrentHashMap<>();

    public record UploadResult(String batchCode, int totalRows, int validRows, int invalidRows,
            List<String> validationErrors) {
    }

    @Transactional
    public UploadResult upload(MultipartFile file, String createdBy) {
        List<BatchItemEntity> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)) {

            int rowNumber = 1;
            for (CSVRecord record : parser) {
                rowNumber++;
                String name = record.isMapped("employee_name") ? record.get("employee_name") : "";
                String employeeId = record.isMapped("employee_id") ? record.get("employee_id") : "";
                String employer = record.isMapped("employer") ? record.get("employer") : "";
                String type = record.isMapped("verification_type") ? record.get("verification_type") : "EMPLOYMENT";

                BatchItemEntity item = new BatchItemEntity();
                item.setRowNumber(rowNumber);
                item.setEmployeeName(name);
                item.setEmployeeNumber(employeeId);
                item.setEmployer(employer);
                item.setVerificationType(type.toUpperCase());

                if (name.isBlank() || employeeId.isBlank()) {
                    item.setStatus("INVALID");
                    item.setFailureReason("employee_name and employee_id are required");
                    errors.add("Row " + rowNumber + ": employee_name and employee_id are required");
                } else {
                    item.setStatus("VALID");
                }
                rows.add(item);
            }
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "CSV could not be parsed. Expected headers: employee_name, employee_id, employer, verification_type");
        }

        if (rows.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "The uploaded file contains no data rows.");
        }
        if (rows.size() > 5000) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Batch size exceeds the 5000-row demo limit.");
        }

        BatchJobEntity job = new BatchJobEntity();
        job.setBatchCode("BATCH-" + Long.toString(Math.abs(UUID.randomUUID().getMostSignificantBits()), 36)
                .toUpperCase().substring(0, 6));
        job.setFileName(file.getOriginalFilename());
        job.setTotalRows(rows.size());
        job.setProcessedRows(0);
        job.setVerifiedRows(0);
        job.setReviewRows(0);
        job.setFailedRows((int) rows.stream().filter(r -> "INVALID".equals(r.getStatus())).count());
        job.setStatus("UPLOADED");
        job.setCreatedBy(createdBy);
        job = jobRepository.save(job);

        for (BatchItemEntity item : rows) {
            item.setBatchId(job.getId());
        }
        itemRepository.saveAll(rows);
        stagedRows.put(job.getId(), rows);

        log.info("batch_uploaded code={} rows={} invalid={}", job.getBatchCode(), rows.size(), errors.size());
        return new UploadResult(job.getBatchCode(), rows.size(), rows.size() - errors.size(), errors.size(), errors);
    }

    /** All batch jobs, newest first — drives the Batch History screen. */
    @Transactional(readOnly = true)
    public List<BatchJobEntity> history() {
        return jobRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /** Submit a validated batch for asynchronous demo processing. */
    @Transactional
    public Map<String, Object> submit(String batchCode) {
        BatchJobEntity job = find(batchCode);
        job.setStatus("PROCESSING");
        jobRepository.save(job);
        processAsync(job.getId());
        return Map.of("batchCode", batchCode, "status", "PROCESSING");
    }

    @Async
    protected void processAsync(UUID batchId) {
        try {
            Thread.sleep(150); // simulate queue latency
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        finalizeProcessing(batchId);
    }

    @Transactional
    public void finalizeProcessing(UUID batchId) {
        BatchJobEntity job = jobRepository.findById(batchId)
                .orElseThrow(() -> new ApiException(ErrorCode.BATCH_NOT_FOUND, "Batch not found."));
        if ("COMPLETED".equals(job.getStatus())) {
            return;
        }
        List<BatchItemEntity> items = itemRepository.findByBatchIdOrderByRowNumberAsc(batchId);
        int verified = 0;
        int review = 0;
        int failed = 0;
        for (BatchItemEntity item : items) {
            if ("INVALID".equals(item.getStatus())) {
                failed++;
                continue;
            }
            // Deterministic demo outcome: employee id starting with DEMO- -> VERIFIED,
            // unknown numbers route to REVIEW (no-hit), anything else 85% verified.
            String result;
            if (item.getEmployeeNumber() != null && item.getEmployeeNumber().startsWith("DEMO-")) {
                result = "VERIFIED";
            } else if (item.getEmployeeNumber() != null && item.getEmployeeNumber().startsWith("UNKNOWN")) {
                result = "REVIEW";
            } else if (Math.abs(item.getRowNumber() * 31) % 100 < 85) {
                result = "VERIFIED";
            } else {
                result = "REVIEW";
            }
            item.setStatus(result);
            item.setResult("VERIFIED".equals(result) ? "VERIFIED" : "VERIFIED WITH REVIEW");
            item.setConfidence(java.math.BigDecimal.valueOf(
                    "VERIFIED".equals(result) ? 95 + (item.getRowNumber() % 5) : 62));
            if ("VERIFIED".equals(result)) {
                verified++;
            } else {
                review++;
            }
        }
        itemRepository.saveAll(items);
        job.setProcessedRows(items.size());
        job.setVerifiedRows(verified);
        job.setReviewRows(review);
        job.setFailedRows(failed);
        job.setStatus("COMPLETED");
        job.setCompletedAt(OffsetDateTime.now());
        jobRepository.save(job);
        log.info("batch_completed code={} verified={} review={} failed={}",
                job.getBatchCode(), verified, review, failed);
    }

    @Transactional(readOnly = true)
    public BatchJobEntity get(String batchCode) {
        return find(batchCode);
    }

    @Transactional(readOnly = true)
    public List<BatchItemEntity> items(String batchCode) {
        return itemRepository.findByBatchIdOrderByRowNumberAsc(find(batchCode).getId());
    }

    @Transactional(readOnly = true)
    public byte[] resultCsv(String batchCode) {
        List<BatchItemEntity> rows = items(batchCode);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            StringBuilder sb = new StringBuilder();
            sb.append("row_number,employee_name,employee_id,employer,verification_type,status,result,confidence\n");
            for (BatchItemEntity i : rows) {
                sb.append(i.getRowNumber()).append(',')
                        .append(csv(i.getEmployeeName())).append(',')
                        .append(csv(i.getEmployeeNumber())).append(',')
                        .append(csv(i.getEmployer())).append(',')
                        .append(csv(i.getVerificationType())).append(',')
                        .append(i.getStatus()).append(',')
                        .append(csv(i.getResult())).append(',')
                        .append(i.getConfidence() == null ? "" : i.getConfidence()).append('\n');
            }
            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            return out.toByteArray();
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Result CSV generation failed.");
        }
    }

    private BatchJobEntity find(String batchCode) {
        return jobRepository.findByBatchCode(batchCode)
                .orElseThrow(() -> new ApiException(ErrorCode.BATCH_NOT_FOUND,
                        "Batch '" + batchCode + "' was not found."));
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return value.contains(",") || value.contains("\"") || value.contains("\n")
                ? "\"" + value.replace("\"", "\"\"") + "\""
                : value;
    }
}
