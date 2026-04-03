package com.payroll.api;

import java.math.BigDecimal;
import java.time.Instant;

import com.payroll.model.PayrollRunStatus;

public record PayrollRunResponse(
        Long id,
        String payrollPeriod,
        PayrollRunStatus status,
        boolean complianceLocked,
        int timesheetCount,
        int anomalyCount,
        BigDecimal totalGrossPay,
        BigDecimal totalNetPay,
        String anomalySummary,
        String bankFileReference,
        String lockedBy,
        Instant createdAt,
        Instant anomalyScannedAt,
        Instant lockedAt,
        Instant payslipsGeneratedAt) {
}

