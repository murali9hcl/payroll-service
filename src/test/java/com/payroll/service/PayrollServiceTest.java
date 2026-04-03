package com.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.payroll.model.PayrollInput;
import com.payroll.model.PayrollResult;

class PayrollServiceTest {

    private final StatePayrollRuleResolver resolver = Mockito.mock(StatePayrollRuleResolver.class);
    private final PayrollService payrollService = new PayrollService(resolver);

    @Test
    void calculatesGrossNetAndDeductions() {
        when(resolver.defaults()).thenReturn(defaultRules());

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
        when(resolver.defaults()).thenReturn(defaultRules());

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
        when(resolver.defaults()).thenReturn(defaultRules());

        PayrollInput input = new PayrollInput();
        input.setHoursWorked(new BigDecimal("-1.00"));
        input.setOvertimeHours(BigDecimal.ZERO);
        input.setHourlyRate(new BigDecimal("20.00"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> payrollService.calculate(input));

        assertTrue(exception.getMessage().contains("hoursWorked"));
    }

    @Test
    void usesConfiguredMinimumWage() {
        PayrollRulesSnapshot customRules = new PayrollRulesSnapshot(
                new BigDecimal("1.5"),
                new BigDecimal("0.0765"),
                new BigDecimal("0.006"),
                new BigDecimal("0.027"),
                new BigDecimal("0.10"),
                new BigDecimal("10.00"),
                new BigDecimal("40.00"),
                new BigDecimal("45.00"),
                new BigDecimal("60.00"),
                new BigDecimal("15.00"),
                new BigDecimal("32.00"),
                new BigDecimal("40.00"));

        when(resolver.resolveForStateAndDate("TX", LocalDate.of(2026, 4, 3))).thenReturn(customRules);

        PayrollInput input = new PayrollInput();
        input.setHoursWorked(new BigDecimal("1.00"));
        input.setOvertimeHours(BigDecimal.ZERO);
        input.setHourlyRate(new BigDecimal("10.00"));
        input.setStateCode("TX");
        input.setAsOfDate(LocalDate.of(2026, 4, 3));

        PayrollResult result = payrollService.calculate(input);

        assertTrue(result.isMinimumWageValid());
    }

    private static PayrollRulesSnapshot defaultRules() {
        return new PayrollRulesSnapshot(
                new BigDecimal("1.5"),
                new BigDecimal("0.0765"),
                new BigDecimal("0.006"),
                new BigDecimal("0.027"),
                new BigDecimal("0.10"),
                new BigDecimal("14.00"),
                new BigDecimal("40.00"),
                new BigDecimal("45.00"),
                new BigDecimal("60.00"),
                new BigDecimal("15.00"),
                new BigDecimal("32.00"),
                new BigDecimal("40.00"));
    }
}

