package com.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payroll.model.Timesheet;
import com.payroll.model.TimesheetStatus;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findAllByOrderBySubmittedAtDesc();

    List<Timesheet> findByPayrollPeriodOrderBySubmittedAtAsc(String payrollPeriod);

    List<Timesheet> findByPayrollPeriodAndStatusOrderBySubmittedAtAsc(String payrollPeriod, TimesheetStatus status);

    List<Timesheet> findByPayrollRunIdOrderByEmployeeIdAsc(Long payrollRunId);
}

