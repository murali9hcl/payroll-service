package com.payroll.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.payroll.model.TimesheetStatus;

public record TimesheetResponse(
        Long id,
        String employeeId,
        String payrollPeriod,
        LocalDate weekStart,
        LocalDate weekEnd,
        BigDecimal hoursWorked,
        BigDecimal overtimeHours,
        BigDecimal hourlyRate,
        TimesheetStatus status,
        boolean overtimeFlag,
        boolean aiRiskWarning,
        String approvalNotes,
        String approvedBy,
        Instant submittedAt,
        Instant decisionAt,
        Long payrollRunId) {
}

