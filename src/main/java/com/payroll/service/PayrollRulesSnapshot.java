package com.payroll.service;

import java.math.BigDecimal;

public record PayrollRulesSnapshot(
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
        BigDecimal overtimeThreshold) {
}

