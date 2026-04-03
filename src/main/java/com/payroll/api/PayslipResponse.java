package com.payroll.api;

import java.math.BigDecimal;
import java.time.Instant;

public record PayslipResponse(
        Long id,
        Long payrollRunId,
        String employeeId,
        BigDecimal grossPay,
        BigDecimal fica,
        BigDecimal futa,
        BigDecimal suta,
        BigDecimal federalTax,
        BigDecimal totalDeductions,
        BigDecimal netPay,
        String storagePath,
        String downloadUrl,
        Instant generatedAt) {
}

