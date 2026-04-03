package com.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payroll.model.Payslip;

public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByPayrollRunIdOrderByEmployeeIdAsc(Long payrollRunId);

    Optional<Payslip> findByPayrollRunIdAndEmployeeId(Long payrollRunId, String employeeId);
}

