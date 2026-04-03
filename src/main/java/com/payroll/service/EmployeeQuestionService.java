package com.payroll.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payroll.api.QuestionRequest;
import com.payroll.api.QuestionResponse;
import com.payroll.exception.ResourceNotFoundException;
import com.payroll.model.EmployeeQuestion;
import com.payroll.model.PayrollRun;
import com.payroll.model.Payslip;
import com.payroll.repository.EmployeeQuestionRepository;
import com.payroll.repository.PayrollRunRepository;
import com.payroll.repository.PayslipRepository;

@Service
@Transactional
public class EmployeeQuestionService {

    private final EmployeeQuestionRepository employeeQuestionRepository;
    private final PayrollRunRepository payrollRunRepository;
    private final PayslipRepository payslipRepository;

    public EmployeeQuestionService(
            EmployeeQuestionRepository employeeQuestionRepository,
            PayrollRunRepository payrollRunRepository,
            PayslipRepository payslipRepository) {
        this.employeeQuestionRepository = employeeQuestionRepository;
        this.payrollRunRepository = payrollRunRepository;
        this.payslipRepository = payslipRepository;
    }

    public QuestionResponse ask(QuestionRequest request) {
        PayrollRun payrollRun = null;
        if (request.payrollRunId() != null) {
            payrollRun = payrollRunRepository.findById(request.payrollRunId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payroll run " + request.payrollRunId() + " was not found"));
        }

        String answer = buildAnswer(request, payrollRun);

        EmployeeQuestion question = new EmployeeQuestion();
        question.setEmployeeId(request.employeeId().trim());
        question.setQuestion(request.question().trim());
        question.setAnswer(answer);
        question.setCreatedAt(java.time.Instant.now());
        question.setPayrollRun(payrollRun);

        return toResponse(employeeQuestionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listForEmployee(String employeeId) {
        return employeeQuestionRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String buildAnswer(QuestionRequest request, PayrollRun payrollRun) {
        String normalizedQuestion = request.question().toLowerCase(Locale.ROOT);
        Optional<Payslip> payslip = payrollRun == null
                ? Optional.empty()
                : payslipRepository.findByPayrollRunIdAndEmployeeId(payrollRun.getId(), request.employeeId().trim());

        if (payslip.isPresent() && (normalizedQuestion.contains("why") || normalizedQuestion.contains("different"))) {
            Payslip currentPayslip = payslip.get();
            return "Your net pay is " + currentPayslip.getNetPay()
                    + " because gross pay " + currentPayslip.getGrossPay()
                    + " had deductions for FICA " + currentPayslip.getFica()
                    + ", FUTA " + currentPayslip.getFuta()
                    + ", SUTA " + currentPayslip.getSuta()
                    + ", and federal tax " + currentPayslip.getFederalTax() + ".";
        }

        if (payrollRun != null && payslip.isEmpty()) {
            return "Your payroll run is recorded, but a finalized payslip is not available yet. Once HR locks the run and generates documents, the payslip download link will appear in the payroll run response.";
        }

        return "HR Copilot placeholder: we captured your question and would normally use payroll context plus approved policies to answer it. If your concern is about pay differences, include the payroll run id for a detailed explanation.";
    }

    private QuestionResponse toResponse(EmployeeQuestion question) {
        return new QuestionResponse(
                question.getId(),
                question.getEmployeeId(),
                question.getPayrollRun() != null ? question.getPayrollRun().getId() : null,
                question.getQuestion(),
                question.getAnswer(),
                question.getCreatedAt());
    }
}

