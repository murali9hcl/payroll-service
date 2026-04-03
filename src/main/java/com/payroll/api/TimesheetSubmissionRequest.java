package com.payroll.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TimesheetSubmissionRequest(
        @NotBlank String employeeId,
        @NotBlank String stateCode,
        @NotBlank String payrollPeriod,
        @NotNull LocalDate weekStart,
        @NotNull LocalDate weekEnd,
        @NotNull @DecimalMin(value = "0.00") BigDecimal hoursWorked,
        @NotNull @DecimalMin(value = "0.00") BigDecimal overtimeHours,
        @NotNull @DecimalMin(value = "0.00") BigDecimal hourlyRate) {
}

