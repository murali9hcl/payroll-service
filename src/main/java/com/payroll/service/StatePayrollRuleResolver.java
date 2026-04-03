package com.payroll.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payroll.config.PayrollRulesProperties;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.model.StatePayrollRule;
import com.payroll.repository.StatePayrollRuleRepository;

@Service
public class StatePayrollRuleResolver {

    private final PayrollRulesProperties defaults;
    private final StatePayrollRuleRepository statePayrollRuleRepository;

    public StatePayrollRuleResolver(
            PayrollRulesProperties defaults,
            StatePayrollRuleRepository statePayrollRuleRepository) {
        this.defaults = defaults;
        this.statePayrollRuleRepository = statePayrollRuleRepository;
    }

    @Transactional(readOnly = true)
    public PayrollRulesSnapshot resolveForStateAndDate(String stateCode, LocalDate asOfDate) {
        if (stateCode == null || stateCode.isBlank()) {
            throw new IllegalArgumentException("stateCode is required");
        }
        if (asOfDate == null) {
            throw new IllegalArgumentException("asOfDate is required");
        }

        List<StatePayrollRule> matches = statePayrollRuleRepository.findEffectiveRules(stateCode.trim(), asOfDate);
        if (matches.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No effective payroll rule found for state " + stateCode.trim().toUpperCase() + " on " + asOfDate);
        }
        return toSnapshot(matches.get(0));
    }

    public PayrollRulesSnapshot defaults() {
        return new PayrollRulesSnapshot(
                defaults.getOvertimeRateMultiplier(),
                defaults.getFicaRate(),
                defaults.getFutaRate(),
                defaults.getSutaRate(),
                defaults.getFederalTaxRate(),
                defaults.getMinimumWage(),
                defaults.getStandardWeekHours(),
                defaults.getExcessiveHoursFlag(),
                defaults.getExcessiveHoursBlock(),
                defaults.getOvertimeWarningHours(),
                defaults.getAiWarningThreshold(),
                defaults.getOvertimeThreshold());
    }

    private PayrollRulesSnapshot toSnapshot(StatePayrollRule rule) {
        return new PayrollRulesSnapshot(
                rule.getOvertimeRateMultiplier(),
                rule.getFicaRate(),
                rule.getFutaRate(),
                rule.getSutaRate(),
                rule.getFederalTaxRate(),
                rule.getMinimumWage(),
                rule.getStandardWeekHours(),
                rule.getExcessiveHoursFlag(),
                rule.getExcessiveHoursBlock(),
                rule.getOvertimeWarningHours(),
                rule.getAiWarningThreshold(),
                rule.getOvertimeThreshold());
    }
}

