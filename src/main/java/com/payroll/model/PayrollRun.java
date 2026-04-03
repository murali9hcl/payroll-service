package com.payroll.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payroll_runs")
@Getter
@Setter
public class PayrollRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String payrollPeriod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PayrollRunStatus status;

    @Column(nullable = false)
    private boolean complianceLocked;

    private Instant createdAt;

    private Instant anomalyScannedAt;

    private Instant lockedAt;

    private Instant payslipsGeneratedAt;

    @Column(length = 100)
    private String lockedBy;

    @Column(length = 1000)
    private String lockReason;

    @Column(length = 2000)
    private String anomalySummary;

    @Column(nullable = false)
    private int anomalyCount;

    @Column(length = 255)
    private String bankFileReference;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL)
    private List<Payslip> payslips = new ArrayList<>();
}

