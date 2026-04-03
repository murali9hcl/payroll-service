package com.payroll.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payroll.api.StatePayrollRuleResponse;
import com.payroll.api.StatePayrollRuleUpsertRequest;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.model.StatePayrollRule;
import com.payroll.repository.StatePayrollRuleRepository;

@Service
@Transactional
public class StatePayrollRuleAdminService {

    private static final LocalDate FAR_FUTURE = LocalDate.of(9999, 12, 31);

    private final StatePayrollRuleRepository statePayrollRuleRepository;

    public StatePayrollRuleAdminService(StatePayrollRuleRepository statePayrollRuleRepository) {
        this.statePayrollRuleRepository = statePayrollRuleRepository;
    }

    public StatePayrollRuleResponse create(StatePayrollRuleUpsertRequest request) {
        validateDateRange(request.effectiveStart(), request.effectiveEnd());
        String stateCode = normalizeStateCode(request.stateCode());
        ensureNoOverlap(stateCode, request.effectiveStart(), request.effectiveEnd(), null);

        StatePayrollRule saved = statePayrollRuleRepository.save(toEntity(new StatePayrollRule(), stateCode, request));
        return toResponse(saved);
    }

    public StatePayrollRuleResponse update(Long id, StatePayrollRuleUpsertRequest request) {
        validateDateRange(request.effectiveStart(), request.effectiveEnd());
        String stateCode = normalizeStateCode(request.stateCode());
        ensureNoOverlap(stateCode, request.effectiveStart(), request.effectiveEnd(), id);

        StatePayrollRule existing = statePayrollRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("State payroll rule " + id + " was not found"));

        return toResponse(statePayrollRuleRepository.save(toEntity(existing, stateCode, request)));
    }

    @Transactional(readOnly = true)
    public List<StatePayrollRuleResponse> list(String stateCode) {
        if (stateCode == null || stateCode.isBlank()) {
            return statePayrollRuleRepository.findAll().stream()
                    .map(this::toResponse)
                    .toList();
        }
        return statePayrollRuleRepository.findByStateCodeIgnoreCaseOrderByEffectiveStartDesc(stateCode.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void ensureNoOverlap(String stateCode, LocalDate effectiveStart, LocalDate effectiveEnd, Long excludeId) {
        LocalDate rangeEnd = effectiveEnd == null ? FAR_FUTURE : effectiveEnd;
        boolean overlaps = statePayrollRuleRepository.hasOverlappingRange(
                stateCode,
                effectiveStart,
                rangeEnd,
                excludeId);
        if (overlaps) {
            throw new IllegalArgumentException(
                    "Overlapping effective date range for state " + stateCode + " is not allowed");
        }
    }

    private static void validateDateRange(LocalDate effectiveStart, LocalDate effectiveEnd) {
        if (effectiveEnd != null && effectiveEnd.isBefore(effectiveStart)) {
            throw new IllegalArgumentException("effectiveEnd must be on or after effectiveStart");
        }
    }

    private static String normalizeStateCode(String stateCode) {
        String normalized = stateCode.trim().toUpperCase();
        if (normalized.length() != 2) {
            throw new IllegalArgumentException("stateCode must be a 2-letter code");
        }
        return normalized;
    }

    private StatePayrollRule toEntity(
            StatePayrollRule entity,
            String stateCode,
            StatePayrollRuleUpsertRequest request) {
        entity.setStateCode(stateCode);
        entity.setEffectiveStart(request.effectiveStart());
        entity.setEffectiveEnd(request.effectiveEnd());
        entity.setOvertimeRateMultiplier(request.overtimeRateMultiplier());
        entity.setFicaRate(request.ficaRate());
        entity.setFutaRate(request.futaRate());
        entity.setSutaRate(request.sutaRate());
        entity.setFederalTaxRate(request.federalTaxRate());
        entity.setMinimumWage(request.minimumWage());
        entity.setStandardWeekHours(request.standardWeekHours());
        entity.setExcessiveHoursFlag(request.excessiveHoursFlag());
        entity.setExcessiveHoursBlock(request.excessiveHoursBlock());
        entity.setOvertimeWarningHours(request.overtimeWarningHours());
        entity.setAiWarningThreshold(request.aiWarningThreshold());
        entity.setOvertimeThreshold(request.overtimeThreshold());
        return entity;
    }

    private StatePayrollRuleResponse toResponse(StatePayrollRule rule) {
        return new StatePayrollRuleResponse(
                rule.getId(),
                rule.getStateCode(),
                rule.getEffectiveStart(),
                rule.getEffectiveEnd(),
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
                rule.getOvertimeThreshold(),
                rule.getCreatedAt());
    }
}

