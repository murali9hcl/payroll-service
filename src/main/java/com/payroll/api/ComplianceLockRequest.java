package com.payroll.api;

import jakarta.validation.constraints.NotBlank;

public record ComplianceLockRequest(
        @NotBlank String reviewedBy,
        @NotBlank String reason) {
}

