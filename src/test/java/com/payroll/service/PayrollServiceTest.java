package com.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.payroll.model.PayrollInput;
import com.payroll.model.PayrollResult;

class PayrollServiceTest {

    private final PayrollService payrollService = new PayrollService();

    @Test
    void calculatesGrossNetAndDeductions() {
        PayrollInput input = new PayrollInput();
        input.setHoursWorked(new BigDecimal("40.00"));
        input.setOvertimeHours(new BigDecimal("5.00"));
        input.setHourlyRate(new BigDecimal("20.00"));

        PayrollResult result = payrollService.calculate(input);

        assertTrue(result.isMinimumWageValid());
        assertEquals(new BigDecimal("950.00"), result.getGrossPay());
        assertEquals(new BigDecimal("72.68"), result.getFica());
        assertEquals(new BigDecimal("5.70"), result.getFuta());
        assertEquals(new BigDecimal("25.65"), result.getSuta());
        assertEquals(new BigDecimal("95.00"), result.getFederalTax());
        assertEquals(new BigDecimal("199.03"), result.getTotalDeductions());
        assertEquals(new BigDecimal("750.98"), result.getNetPay());
    }

    @Test
    void marksBelowMinimumWageWithoutThrowing() {
        PayrollInput input = new PayrollInput();
        input.setHoursWorked(new BigDecimal("40.00"));
        input.setOvertimeHours(BigDecimal.ZERO);
        input.setHourlyRate(new BigDecimal("10.00"));

        PayrollResult result = payrollService.calculate(input);

        assertFalse(result.isMinimumWageValid());
        assertEquals(new BigDecimal("400.00"), result.getGrossPay());
    }

    @Test
    void rejectsNegativeAmounts() {
        PayrollInput input = new PayrollInput();
        input.setHoursWorked(new BigDecimal("-1.00"));
        input.setOvertimeHours(BigDecimal.ZERO);
        input.setHourlyRate(new BigDecimal("20.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> payrollService.calculate(input));

        assertTrue(exception.getMessage().contains("hoursWorked"));
    }
}

