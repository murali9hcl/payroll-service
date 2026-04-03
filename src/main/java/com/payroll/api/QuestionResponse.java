package com.payroll.api;

import java.time.Instant;

public record QuestionResponse(
        Long id,
        String employeeId,
        Long payrollRunId,
        String question,
        String answer,
        Instant createdAt) {
}

