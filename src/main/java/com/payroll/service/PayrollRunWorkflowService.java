package com.payroll.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payroll.api.AnomalyResponse;
import com.payroll.api.ComplianceLockRequest;
import com.payroll.api.PayrollRunRequest;
import com.payroll.api.PayrollRunResponse;
import com.payroll.api.PayslipResponse;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.model.AnomalySeverity;
import com.payroll.model.PayrollInput;
import com.payroll.model.PayrollResult;
import com.payroll.model.PayrollRun;
import com.payroll.model.PayrollRunStatus;
import com.payroll.model.Payslip;
import com.payroll.model.Timesheet;
import com.payroll.model.TimesheetStatus;
import com.payroll.repository.PayrollRunRepository;
import com.payroll.repository.PayslipRepository;
import com.payroll.repository.TimesheetRepository;

@Service
@Transactional
public class PayrollRunWorkflowService {

    private final PayrollRunRepository payrollRunRepository;
    private final TimesheetRepository timesheetRepository;
    private final PayslipRepository payslipRepository;
    private final PayrollService payrollService;
    private final StatePayrollRuleResolver statePayrollRuleResolver;

    public PayrollRunWorkflowService(
            PayrollRunRepository payrollRunRepository,
            TimesheetRepository timesheetRepository,
            PayslipRepository payslipRepository,
            PayrollService payrollService,
            StatePayrollRuleResolver statePayrollRuleResolver) {
        this.payrollRunRepository = payrollRunRepository;
        this.timesheetRepository = timesheetRepository;
        this.payslipRepository = payslipRepository;
        this.payrollService = payrollService;
        this.statePayrollRuleResolver = statePayrollRuleResolver;
    }

    public PayrollRunResponse createRun(PayrollRunRequest request) {
        payrollRunRepository.findByPayrollPeriod(request.payrollPeriod().trim())
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Payroll run already exists for period " + request.payrollPeriod().trim());
                });

        List<Timesheet> approvedTimesheets = timesheetRepository.findByPayrollPeriodAndStatusOrderBySubmittedAtAsc(
                request.payrollPeriod().trim(),
                TimesheetStatus.APPROVED);

        if (approvedTimesheets.isEmpty()) {
            throw new IllegalArgumentException(
                    "No approved timesheets found for payroll period " + request.payrollPeriod().trim());
        }

        PayrollRun run = new PayrollRun();
        run.setPayrollPeriod(request.payrollPeriod().trim());
        run.setStatus(PayrollRunStatus.CALCULATED);
        run.setComplianceLocked(false);
        run.setCreatedAt(Instant.now());
        run.setAnomalySummary("Pending anomaly scan");
        run.setAnomalyCount(0);
        payrollRunRepository.save(run);

        approvedTimesheets.forEach(timesheet -> {
            timesheet.setPayrollRun(run);
            timesheet.setStatus(TimesheetStatus.INCLUDED_IN_PAYROLL);
        });
        timesheetRepository.saveAll(approvedTimesheets);

        return toRunResponse(run);
    }

    @Transactional(readOnly = true)
    public PayrollRunResponse getRun(Long id) {
        return toRunResponse(findRun(id));
    }

    public List<AnomalyResponse> scanAnomalies(Long payrollRunId) {
        PayrollRun run = findRun(payrollRunId);
        List<Timesheet> timesheets = timesheetRepository.findByPayrollRunIdOrderByEmployeeIdAsc(payrollRunId);
        if (timesheets.isEmpty()) {
            throw new IllegalStateException("Payroll run " + payrollRunId + " has no included timesheets");
        }

        List<AnomalyResponse> anomalies = new ArrayList<>();
        for (Timesheet timesheet : timesheets) {
            BigDecimal totalHours = timesheet.getHoursWorked().add(timesheet.getOvertimeHours());
            PayrollRulesSnapshot ruleSet = statePayrollRuleResolver.resolveForStateAndDate(
                    timesheet.getStateCode(),
                    timesheet.getWeekEnd());
            if (!payrollService.isMinimumWageValid(timesheet.getHourlyRate(), ruleSet)) {
                anomalies.add(new AnomalyResponse(
                        timesheet.getId(),
                        timesheet.getEmployeeId(),
                        "Hourly rate below configured minimum wage threshold for the state",
                        AnomalySeverity.BLOCK,
                        ruleSet.minimumWage(),
                        timesheet.getHourlyRate()));
            }
            if (totalHours.compareTo(ruleSet.excessiveHoursBlock()) > 0) {
                anomalies.add(new AnomalyResponse(
                        timesheet.getId(),
                        timesheet.getEmployeeId(),
                        "Hours are high enough to block payroll pending HR review",
                        AnomalySeverity.BLOCK,
                        ruleSet.standardWeekHours(),
                        totalHours));
            } else if (totalHours.compareTo(ruleSet.excessiveHoursFlag()) > 0) {
                anomalies.add(new AnomalyResponse(
                        timesheet.getId(),
                        timesheet.getEmployeeId(),
                        "Hours exceed the normal work week and should be reviewed",
                        AnomalySeverity.FLAG,
                        ruleSet.standardWeekHours(),
                        totalHours));
            }
            if (timesheet.getOvertimeHours().compareTo(ruleSet.overtimeWarningHours()) > 0) {
                anomalies.add(new AnomalyResponse(
                        timesheet.getId(),
                        timesheet.getEmployeeId(),
                        "Overtime exceeds the threshold used by the anomaly agent",
                        AnomalySeverity.FLAG,
                        ruleSet.overtimeWarningHours(),
                        timesheet.getOvertimeHours()));
            }
        }

        run.setAnomalyScannedAt(Instant.now());
        run.setAnomalyCount(anomalies.size());
        if (anomalies.isEmpty()) {
            run.setStatus(PayrollRunStatus.CALCULATED);
            run.setAnomalySummary("No anomalies detected");
        } else {
            run.setStatus(PayrollRunStatus.ANOMALIES_FLAGGED);
            long blocks = anomalies.stream().filter(anomaly -> anomaly.severity() == AnomalySeverity.BLOCK).count();
            long flags = anomalies.size() - blocks;
            run.setAnomalySummary("Anomalies detected: " + blocks + " BLOCK, " + flags + " FLAG");
        }
        payrollRunRepository.save(run);

        return anomalies;
    }

    public PayrollRunResponse lockRun(Long payrollRunId, ComplianceLockRequest request) {
        PayrollRun run = findRun(payrollRunId);
        if (run.getAnomalyScannedAt() == null) {
            throw new IllegalStateException("Anomaly scan must be completed before compliance lock");
        }

        run.setComplianceLocked(true);
        run.setLockedBy(request.reviewedBy().trim());
        run.setLockReason(request.reason().trim());
        run.setLockedAt(Instant.now());
        run.setStatus(PayrollRunStatus.LOCKED);

        return toRunResponse(payrollRunRepository.save(run));
    }

    public List<PayslipResponse> generatePayslips(Long payrollRunId) {
        PayrollRun run = findRun(payrollRunId);
        if (!run.isComplianceLocked()) {
            throw new IllegalStateException("Compliance HR must lock the payroll run before payslips are generated");
        }

        List<Payslip> existingPayslips = payslipRepository.findByPayrollRunIdOrderByEmployeeIdAsc(payrollRunId);
        if (!existingPayslips.isEmpty()) {
            return existingPayslips.stream().map(this::toPayslipResponse).toList();
        }

        List<Timesheet> timesheets = timesheetRepository.findByPayrollRunIdOrderByEmployeeIdAsc(payrollRunId);
        if (timesheets.isEmpty()) {
            throw new IllegalStateException("Payroll run " + payrollRunId + " has no included timesheets");
        }

        Instant generatedAt = Instant.now();
        String bankFileReference = "ach/" + run.getPayrollPeriod() + "/run-" + run.getId() + ".ach";
        List<Payslip> payslips = timesheets.stream()
                .map(timesheet -> buildPayslip(run, timesheet, bankFileReference, generatedAt))
                .toList();

        List<Payslip> savedPayslips = payslipRepository.saveAll(payslips);
        run.setStatus(PayrollRunStatus.PAYSLIPS_GENERATED);
        run.setPayslipsGeneratedAt(generatedAt);
        run.setBankFileReference(bankFileReference);
        payrollRunRepository.save(run);

        return savedPayslips.stream().map(this::toPayslipResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> listPayslips(Long payrollRunId) {
        findRun(payrollRunId);
        return payslipRepository.findByPayrollRunIdOrderByEmployeeIdAsc(payrollRunId)
                .stream()
                .map(this::toPayslipResponse)
                .toList();
    }

    private Payslip buildPayslip(PayrollRun run, Timesheet timesheet, String bankFileReference, Instant generatedAt) {
        PayrollRulesSnapshot ruleSet = statePayrollRuleResolver.resolveForStateAndDate(
                timesheet.getStateCode(),
                timesheet.getWeekEnd());
        PayrollResult result = payrollService.calculate(toInput(timesheet), ruleSet);

        Payslip payslip = new Payslip();
        payslip.setPayrollRun(run);
        payslip.setEmployeeId(timesheet.getEmployeeId());
        payslip.setGrossPay(result.getGrossPay());
        payslip.setFica(result.getFica());
        payslip.setFuta(result.getFuta());
        payslip.setSuta(result.getSuta());
        payslip.setFederalTax(result.getFederalTax());
        payslip.setTotalDeductions(result.getTotalDeductions());
        payslip.setNetPay(result.getNetPay());
        payslip.setHoursWorked(timesheet.getHoursWorked());
        payslip.setOvertimeHours(timesheet.getOvertimeHours());
        payslip.setHourlyRate(timesheet.getHourlyRate());
        payslip.setGeneratedAt(generatedAt);
        payslip.setStoragePath("s3://payroll-service/" + run.getPayrollPeriod() + "/" + timesheet.getEmployeeId() + ".pdf");
        payslip.setDownloadUrl("https://documents.payroll.local/" + run.getPayrollPeriod() + "/"
                + timesheet.getEmployeeId() + ".pdf?signature=demo-" + run.getId());
        return payslip;
    }

    private PayrollInput toInput(Timesheet timesheet) {
        PayrollInput input = new PayrollInput();
        input.setHoursWorked(timesheet.getHoursWorked());
        input.setOvertimeHours(timesheet.getOvertimeHours());
        input.setHourlyRate(timesheet.getHourlyRate());
        input.setStateCode(timesheet.getStateCode());
        input.setAsOfDate(timesheet.getWeekEnd());
        return input;
    }

    private PayrollRun findRun(Long id) {
        return payrollRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run " + id + " was not found"));
    }

    private PayrollRunResponse toRunResponse(PayrollRun run) {
        List<Timesheet> timesheets = timesheetRepository.findByPayrollRunIdOrderByEmployeeIdAsc(run.getId());
        BigDecimal totalGrossPay = BigDecimal.ZERO;
        BigDecimal totalNetPay = BigDecimal.ZERO;
        for (Timesheet timesheet : timesheets) {
            PayrollRulesSnapshot ruleSet = statePayrollRuleResolver.resolveForStateAndDate(
                    timesheet.getStateCode(),
                    timesheet.getWeekEnd());
            PayrollResult result = payrollService.calculate(toInput(timesheet), ruleSet);
            totalGrossPay = totalGrossPay.add(result.getGrossPay());
            totalNetPay = totalNetPay.add(result.getNetPay());
        }

        return new PayrollRunResponse(
                run.getId(),
                run.getPayrollPeriod(),
                run.getStatus(),
                run.isComplianceLocked(),
                timesheets.size(),
                run.getAnomalyCount(),
                totalGrossPay,
                totalNetPay,
                run.getAnomalySummary(),
                run.getBankFileReference(),
                run.getLockedBy(),
                run.getCreatedAt(),
                run.getAnomalyScannedAt(),
                run.getLockedAt(),
                run.getPayslipsGeneratedAt());
    }

    private PayslipResponse toPayslipResponse(Payslip payslip) {
        return new PayslipResponse(
                payslip.getId(),
                payslip.getPayrollRun().getId(),
                payslip.getEmployeeId(),
                payslip.getGrossPay(),
                payslip.getFica(),
                payslip.getFuta(),
                payslip.getSuta(),
                payslip.getFederalTax(),
                payslip.getTotalDeductions(),
                payslip.getNetPay(),
                payslip.getStoragePath(),
                payslip.getDownloadUrl(),
                payslip.getGeneratedAt());
    }
}

