package com.payroll.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record StatePayrollRuleResponse(
        Long id,
        String stateCode,
        LocalDate effectiveStart,
        LocalDate effectiveEnd,
        BigDecimal overtimeRateMultiplier,
        BigDecimal ficaRate,
        BigDecimal futaRate,
        BigDecimal sutaRate,
        BigDecimal federalTaxRate,
        BigDecimal minimumWage,
        BigDecimal standardWeekHours,
        BigDecimal excessiveHoursFlag,
        BigDecimal excessiveHoursBlock,
        BigDecimal overtimeWarningHours,
        BigDecimal aiWarningThreshold,
        BigDecimal overtimeThreshold,
        Instant createdAt) {
}

