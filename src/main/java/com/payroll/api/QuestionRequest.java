package com.payroll.api;

import jakarta.validation.constraints.NotBlank;

public record QuestionRequest(
        @NotBlank String employeeId,
        Long payrollRunId,
        @NotBlank String question) {
}

