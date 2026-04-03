package com.payroll.api;

import jakarta.validation.constraints.NotBlank;

public record PayrollRunRequest(@NotBlank String payrollPeriod) {
}

