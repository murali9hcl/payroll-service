package com.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.payroll.model.PayrollInput;
import com.payroll.model.PayrollResult;


@Service
public class PayrollService {

    private static final BigDecimal OT_RATE =
            new BigDecimal("1.5");

    private static final BigDecimal FICA_RATE =
            new BigDecimal("0.0765");

    private static final BigDecimal FUTA_RATE =
            new BigDecimal("0.006");

    private static final BigDecimal SUTA_RATE =
            new BigDecimal("0.027");

    private static final BigDecimal FED_TAX_RATE =
            new BigDecimal("0.10");

    private static final BigDecimal MIN_WAGE =
            new BigDecimal("14.00");

    public PayrollResult calculate(PayrollInput input) {
        Objects.requireNonNull(input, "Payroll input is required");
        validateAmount(input.getHoursWorked(), "hoursWorked");
        validateAmount(input.getOvertimeHours(), "overtimeHours");
        validateAmount(input.getHourlyRate(), "hourlyRate");

        PayrollResult result =
                new PayrollResult();

        boolean minWageValid = isMinimumWageValid(input.getHourlyRate());

        result.setMinimumWageValid(minWageValid);

        BigDecimal regularPay =
                input.getHoursWorked()
                        .multiply(input.getHourlyRate());

        BigDecimal overtimePay =
                input.getOvertimeHours()
                        .multiply(input.getHourlyRate())
                        .multiply(OT_RATE);

        BigDecimal grossPay =
                regularPay.add(overtimePay);

        BigDecimal fica =
                grossPay.multiply(FICA_RATE);

        BigDecimal futa =
                grossPay.multiply(FUTA_RATE);

        BigDecimal suta =
                grossPay.multiply(SUTA_RATE);

        BigDecimal federalTax =
                grossPay.multiply(FED_TAX_RATE);

        BigDecimal totalDeductions =
                fica
                        .add(futa)
                        .add(suta)
                        .add(federalTax);

        BigDecimal netPay =
                grossPay.subtract(totalDeductions);

        result.setGrossPay(scale(grossPay));

        result.setFica(scale(fica));

        result.setFuta(scale(futa));

        result.setSuta(scale(suta));

        result.setFederalTax(scale(federalTax));

        result.setTotalDeductions(
                scale(totalDeductions));

        result.setNetPay(scale(netPay));

        return result;
    }

    public boolean isMinimumWageValid(BigDecimal hourlyRate) {
        validateAmount(hourlyRate, "hourlyRate");
        return hourlyRate.compareTo(MIN_WAGE) >= 0;
    }

    public BigDecimal getMinimumWage() {
        return MIN_WAGE;
    }

    private void validateAmount(BigDecimal value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    private BigDecimal scale(BigDecimal value) {

        return value.setScale(
                2,
                RoundingMode.HALF_UP);
    }

}