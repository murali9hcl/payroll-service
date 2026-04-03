package com.payroll.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payroll.api.TimesheetApprovalRequest;
import com.payroll.api.TimesheetResponse;
import com.payroll.api.TimesheetSubmissionRequest;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.model.Timesheet;
import com.payroll.model.TimesheetStatus;
import com.payroll.repository.TimesheetRepository;

@Service
@Transactional
public class TimesheetWorkflowService {

    private final TimesheetRepository timesheetRepository;
    private final StatePayrollRuleResolver statePayrollRuleResolver;

    public TimesheetWorkflowService(TimesheetRepository timesheetRepository, StatePayrollRuleResolver statePayrollRuleResolver) {
        this.timesheetRepository = timesheetRepository;
        this.statePayrollRuleResolver = statePayrollRuleResolver;
    }

    public TimesheetResponse submit(TimesheetSubmissionRequest request) {
        validateDateRange(request);

        Timesheet timesheet = new Timesheet();
        timesheet.setEmployeeId(request.employeeId().trim());
        timesheet.setStateCode(normalizeStateCode(request.stateCode()));
        timesheet.setPayrollPeriod(request.payrollPeriod().trim());
        timesheet.setWeekStart(request.weekStart());
        timesheet.setWeekEnd(request.weekEnd());
        timesheet.setHoursWorked(request.hoursWorked());
        timesheet.setOvertimeHours(request.overtimeHours());
        timesheet.setHourlyRate(request.hourlyRate());
        timesheet.setStatus(TimesheetStatus.SUBMITTED);
        PayrollRulesSnapshot ruleSet = statePayrollRuleResolver.resolveForStateAndDate(
                timesheet.getStateCode(),
                timesheet.getWeekEnd());
        timesheet.setAiRiskWarning(totalHours(timesheet).compareTo(ruleSet.aiWarningThreshold()) >= 0);
        timesheet.setOvertimeFlag(false);
        timesheet.setSubmittedAt(Instant.now());

        return toResponse(timesheetRepository.save(timesheet));
    }

    @Transactional(readOnly = true)
    public List<TimesheetResponse> list() {
        return timesheetRepository.findAllByOrderBySubmittedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TimesheetResponse get(Long id) {
        return toResponse(findTimesheet(id));
    }

    public TimesheetResponse approve(Long id, TimesheetApprovalRequest request) {
        Timesheet timesheet = findTimesheet(id);
        requireSubmitted(timesheet, "approve");

        BigDecimal totalHours = totalHours(timesheet);
        PayrollRulesSnapshot ruleSet = statePayrollRuleResolver.resolveForStateAndDate(
                timesheet.getStateCode(),
                timesheet.getWeekEnd());
        timesheet.setStatus(TimesheetStatus.APPROVED);
        timesheet.setApprovedBy(request.managerId().trim());
        timesheet.setApprovalNotes(request.notes());
        timesheet.setDecisionAt(Instant.now());
        timesheet.setAiRiskWarning(totalHours.compareTo(ruleSet.aiWarningThreshold()) >= 0);
        timesheet.setOvertimeFlag(totalHours.compareTo(ruleSet.overtimeThreshold()) > 0);

        return toResponse(timesheetRepository.save(timesheet));
    }

    public TimesheetResponse reject(Long id, TimesheetApprovalRequest request) {
        Timesheet timesheet = findTimesheet(id);
        requireSubmitted(timesheet, "reject");

        timesheet.setStatus(TimesheetStatus.REJECTED);
        timesheet.setApprovedBy(request.managerId().trim());
        timesheet.setApprovalNotes(request.notes());
        timesheet.setDecisionAt(Instant.now());

        return toResponse(timesheetRepository.save(timesheet));
    }

    private Timesheet findTimesheet(Long id) {
        return timesheetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timesheet " + id + " was not found"));
    }

    private void requireSubmitted(Timesheet timesheet, String action) {
        if (timesheet.getStatus() != TimesheetStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted timesheets can be used to " + action + ". Current status: "
                    + timesheet.getStatus());
        }
    }

    private void validateDateRange(TimesheetSubmissionRequest request) {
        if (request.weekEnd().isBefore(request.weekStart())) {
            throw new IllegalArgumentException("weekEnd must be on or after weekStart");
        }
    }

    private BigDecimal totalHours(Timesheet timesheet) {
        return timesheet.getHoursWorked().add(timesheet.getOvertimeHours());
    }

    private String normalizeStateCode(String stateCode) {
        String normalized = stateCode.trim().toUpperCase();
        if (normalized.length() != 2) {
            throw new IllegalArgumentException("stateCode must be a 2-letter code");
        }
        return normalized;
    }

    private TimesheetResponse toResponse(Timesheet timesheet) {
        return new TimesheetResponse(
                timesheet.getId(),
                timesheet.getEmployeeId(),
                timesheet.getStateCode(),
                timesheet.getPayrollPeriod(),
                timesheet.getWeekStart(),
                timesheet.getWeekEnd(),
                timesheet.getHoursWorked(),
                timesheet.getOvertimeHours(),
                timesheet.getHourlyRate(),
                timesheet.getStatus(),
                timesheet.isOvertimeFlag(),
                timesheet.isAiRiskWarning(),
                timesheet.getApprovalNotes(),
                timesheet.getApprovedBy(),
                timesheet.getSubmittedAt(),
                timesheet.getDecisionAt(),
                timesheet.getPayrollRun() != null ? timesheet.getPayrollRun().getId() : null);
    }
}

