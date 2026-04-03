package com.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "timesheets")
@Getter
@Setter
public class Timesheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String employeeId;

    @Column(nullable = false, length = 2)
    private String stateCode;

    @Column(nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false)
    private LocalDate weekEnd;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeHours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, length = 30)
    private String payrollPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TimesheetStatus status;

    @Column(nullable = false)
    private boolean overtimeFlag;

    @Column(nullable = false)
    private boolean aiRiskWarning;

    @Column(length = 1000)
    private String approvalNotes;

    @Column(length = 100)
    private String approvedBy;

    private Instant submittedAt;

    private Instant decisionAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id")
    private PayrollRun payrollRun;
}

