package com.payroll.api;

import jakarta.validation.constraints.NotBlank;

public record TimesheetApprovalRequest(
        @NotBlank String managerId,
        String notes) {
}

