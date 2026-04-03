package com.payroll.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.payroll.model.PayrollInput;
import com.payroll.model.PayrollResult;


@Service
public class PayrollService {

    private final StatePayrollRuleResolver statePayrollRuleResolver;

    public PayrollService(StatePayrollRuleResolver statePayrollRuleResolver) {
        this.statePayrollRuleResolver = statePayrollRuleResolver;
    }

    public PayrollResult calculate(PayrollInput input) {
        PayrollRulesSnapshot rules = resolveRulesForInput(input);
        return calculate(input, rules);
    }

    public PayrollResult calculate(PayrollInput input, PayrollRulesSnapshot rules) {
        Objects.requireNonNull(input, "Payroll input is required");
        validateAmount(input.getHoursWorked(), "hoursWorked");
        validateAmount(input.getOvertimeHours(), "overtimeHours");
        validateAmount(input.getHourlyRate(), "hourlyRate");

        PayrollResult result =
                new PayrollResult();

        boolean minWageValid = isMinimumWageValid(input.getHourlyRate(), rules);

        result.setMinimumWageValid(minWageValid);

        BigDecimal regularPay =
                input.getHoursWorked()
                        .multiply(input.getHourlyRate());

        BigDecimal overtimePay =
                input.getOvertimeHours()
                        .multiply(input.getHourlyRate())
                        .multiply(rules.overtimeRateMultiplier());

        BigDecimal grossPay =
                regularPay.add(overtimePay);

        BigDecimal fica =
                grossPay.multiply(rules.ficaRate());

        BigDecimal futa =
                grossPay.multiply(rules.futaRate());

        BigDecimal suta =
                grossPay.multiply(rules.sutaRate());

        BigDecimal federalTax =
                grossPay.multiply(rules.federalTaxRate());

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
        return isMinimumWageValid(hourlyRate, statePayrollRuleResolver.defaults());
    }

    public boolean isMinimumWageValid(BigDecimal hourlyRate, PayrollRulesSnapshot rules) {
        validateAmount(hourlyRate, "hourlyRate");
        return hourlyRate.compareTo(rules.minimumWage()) >= 0;
    }

    public BigDecimal getMinimumWage() {
        return statePayrollRuleResolver.defaults().minimumWage();
    }

    private PayrollRulesSnapshot resolveRulesForInput(PayrollInput input) {
        if (input == null || input.getStateCode() == null || input.getStateCode().isBlank()) {
            return statePayrollRuleResolver.defaults();
        }
        LocalDate asOfDate = input.getAsOfDate() == null ? LocalDate.now() : input.getAsOfDate();
        return statePayrollRuleResolver.resolveForStateAndDate(input.getStateCode(), asOfDate);
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