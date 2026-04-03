package com.payroll.controller;

import java.util.List;

import jakarta.validation.Valid;

import com.payroll.api.AnomalyResponse;
import com.payroll.api.ComplianceLockRequest;
import com.payroll.api.PayrollRunRequest;
import com.payroll.api.PayrollRunResponse;
import com.payroll.api.PayslipResponse;
import com.payroll.api.QuestionRequest;
import com.payroll.api.QuestionResponse;
import com.payroll.api.StatePayrollRuleResponse;
import com.payroll.api.StatePayrollRuleUpsertRequest;
import com.payroll.api.TimesheetApprovalRequest;
import com.payroll.api.TimesheetResponse;
import com.payroll.api.TimesheetSubmissionRequest;
import com.payroll.model.PayrollInput;
import com.payroll.model.PayrollResult;
import com.payroll.service.EmployeeQuestionService;
import com.payroll.service.PayrollService;
import com.payroll.service.PayrollRunWorkflowService;
import com.payroll.service.StatePayrollRuleAdminService;
import com.payroll.service.TimesheetWorkflowService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payroll")
public class PayrollController {

    private final PayrollService payrollService;
    private final TimesheetWorkflowService timesheetWorkflowService;
    private final PayrollRunWorkflowService payrollRunWorkflowService;
    private final EmployeeQuestionService employeeQuestionService;
    private final StatePayrollRuleAdminService statePayrollRuleAdminService;

    public PayrollController(
            PayrollService payrollService,
            TimesheetWorkflowService timesheetWorkflowService,
            PayrollRunWorkflowService payrollRunWorkflowService,
            EmployeeQuestionService employeeQuestionService,
            StatePayrollRuleAdminService statePayrollRuleAdminService) {
        this.payrollService = payrollService;
        this.timesheetWorkflowService = timesheetWorkflowService;
        this.payrollRunWorkflowService = payrollRunWorkflowService;
        this.employeeQuestionService = employeeQuestionService;
        this.statePayrollRuleAdminService = statePayrollRuleAdminService;
    }

    @PostMapping("/calculate")
    public PayrollResult calculate(@Valid @RequestBody PayrollInput input) {
        return payrollService.calculate(input);
    }

    @PostMapping("/timesheets")
    public TimesheetResponse submitTimesheet(@Valid @RequestBody TimesheetSubmissionRequest request) {
        return timesheetWorkflowService.submit(request);
    }

    @GetMapping("/timesheets")
    public List<TimesheetResponse> listTimesheets() {
        return timesheetWorkflowService.list();
    }

    @GetMapping("/timesheets/{id}")
    public TimesheetResponse getTimesheet(@PathVariable Long id) {
        return timesheetWorkflowService.get(id);
    }

    @PostMapping("/timesheets/{id}/approve")
    public TimesheetResponse approveTimesheet(@PathVariable Long id, @Valid @RequestBody TimesheetApprovalRequest request) {
        return timesheetWorkflowService.approve(id, request);
    }

    @PostMapping("/timesheets/{id}/reject")
    public TimesheetResponse rejectTimesheet(@PathVariable Long id, @Valid @RequestBody TimesheetApprovalRequest request) {
        return timesheetWorkflowService.reject(id, request);
    }

    @PostMapping("/runs")
    public PayrollRunResponse createPayrollRun(@Valid @RequestBody PayrollRunRequest request) {
        return payrollRunWorkflowService.createRun(request);
    }

    @GetMapping("/runs/{id}")
    public PayrollRunResponse getPayrollRun(@PathVariable Long id) {
        return payrollRunWorkflowService.getRun(id);
    }

    @PostMapping("/runs/{id}/scan-anomalies")
    public List<AnomalyResponse> scanAnomalies(@PathVariable Long id) {
        return payrollRunWorkflowService.scanAnomalies(id);
    }

    @PostMapping("/runs/{id}/lock")
    public PayrollRunResponse lockPayrollRun(@PathVariable Long id, @Valid @RequestBody ComplianceLockRequest request) {
        return payrollRunWorkflowService.lockRun(id, request);
    }

    @PostMapping("/runs/{id}/generate-payslips")
    public List<PayslipResponse> generatePayslips(@PathVariable Long id) {
        return payrollRunWorkflowService.generatePayslips(id);
    }

    @GetMapping("/runs/{id}/payslips")
    public List<PayslipResponse> listPayslips(@PathVariable Long id) {
        return payrollRunWorkflowService.listPayslips(id);
    }

    @PostMapping("/questions")
    public QuestionResponse askQuestion(@Valid @RequestBody QuestionRequest request) {
        return employeeQuestionService.ask(request);
    }

    @GetMapping("/questions/{employeeId}")
    public List<QuestionResponse> listQuestions(@PathVariable String employeeId) {
        return employeeQuestionService.listForEmployee(employeeId);
    }

    @PostMapping("/admin/state-rules")
    public StatePayrollRuleResponse createStateRule(@Valid @RequestBody StatePayrollRuleUpsertRequest request) {
        return statePayrollRuleAdminService.create(request);
    }

    @PutMapping("/admin/state-rules/{id}")
    public StatePayrollRuleResponse updateStateRule(
            @PathVariable Long id,
            @Valid @RequestBody StatePayrollRuleUpsertRequest request) {
        return statePayrollRuleAdminService.update(id, request);
    }

    @GetMapping("/admin/state-rules")
    public List<StatePayrollRuleResponse> listStateRules(@RequestParam(required = false) String stateCode) {
        return statePayrollRuleAdminService.list(stateCode);
    }
}