package com.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "payslips")
@Getter
@Setter
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String employeeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal grossPay;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fica;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal futa;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal suta;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal federalTax;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDeductions;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netPay;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeHours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, length = 255)
    private String storagePath;

    @Column(nullable = false, length = 255)
    private String downloadUrl;

    private Instant generatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_run_id", nullable = false)
    private PayrollRun payrollRun;
}

