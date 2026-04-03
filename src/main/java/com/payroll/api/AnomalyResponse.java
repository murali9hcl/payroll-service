package com.payroll.api;

import java.math.BigDecimal;

import com.payroll.model.AnomalySeverity;

public record AnomalyResponse(
        Long timesheetId,
        String employeeId,
        String issue,
        AnomalySeverity severity,
        BigDecimal expected,
        BigDecimal actual) {
}

