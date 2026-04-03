package com.payroll.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StatePayrollRuleUpsertRequest(
        @NotBlank String stateCode,
        @NotNull LocalDate effectiveStart,
        LocalDate effectiveEnd,
        @NotNull @DecimalMin("0.00") BigDecimal overtimeRateMultiplier,
        @NotNull @DecimalMin("0.00") BigDecimal ficaRate,
        @NotNull @DecimalMin("0.00") BigDecimal futaRate,
        @NotNull @DecimalMin("0.00") BigDecimal sutaRate,
        @NotNull @DecimalMin("0.00") BigDecimal federalTaxRate,
        @NotNull @DecimalMin("0.00") BigDecimal minimumWage,
        @NotNull @DecimalMin("0.00") BigDecimal standardWeekHours,
        @NotNull @DecimalMin("0.00") BigDecimal excessiveHoursFlag,
        @NotNull @DecimalMin("0.00") BigDecimal excessiveHoursBlock,
        @NotNull @DecimalMin("0.00") BigDecimal overtimeWarningHours,
        @NotNull @DecimalMin("0.00") BigDecimal aiWarningThreshold,
        @NotNull @DecimalMin("0.00") BigDecimal overtimeThreshold) {
}

