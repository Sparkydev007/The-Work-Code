package com.theworkcode.verification.service;

import java.util.List;

import com.theworkcode.verification.dto.VerificationDtos.AnalyticsResponse;
import com.theworkcode.verification.dto.VerificationDtos.DashboardMetrics;
import com.theworkcode.verification.dto.VerificationDtos.StatusCount;
import com.theworkcode.verification.dto.VerificationDtos.TypeCount;
import com.theworkcode.verification.repository.VerificationRequestRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates verification metrics for the dashboard and analytics screens.
 * Count-based aggregations run in the database, not in the browser.
 */
@Service
@RequiredArgsConstructor
public class VerificationAnalyticsService {

    private final VerificationRequestRepository repository;

    @Transactional(readOnly = true)
    public AnalyticsResponse dashboard() {
        long total = repository.count();
        long completed = repository.countByStatus("COMPLETED");
        long review = repository.countByStatus("REVIEW_REQUIRED");
        long failed = repository.countByStatus("FAILED");
        long pending = total - completed - review - failed;

        long success = completed + review; // review still yields a usable result
        double successRate = total == 0 ? 0 : Math.round(1000.0 * success / total) / 10.0;

        DashboardMetrics metrics = new DashboardMetrics(total, completed, pending, review, failed,
                successRate, 2.4); // avg processing seconds (demo constant, measured in production)

        List<StatusCount> statusDistribution = List.of(
                new StatusCount("COMPLETED", completed),
                new StatusCount("REVIEW_REQUIRED", review),
                new StatusCount("FAILED", failed),
                new StatusCount("PENDING", Math.max(0, pending)));

        List<TypeCount> typeDistribution = List.of(
                new TypeCount("EMPLOYMENT", repository.count()),
                new TypeCount("INCOME", repository.count()),
                new TypeCount("EMPLOYMENT_AND_INCOME", repository.count()));

        return new AnalyticsResponse(metrics, statusDistribution, typeDistribution);
    }
}
