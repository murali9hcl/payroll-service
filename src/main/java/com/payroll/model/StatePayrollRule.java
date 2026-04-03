package com.payroll.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "state_payroll_rules")
@Getter
@Setter
public class StatePayrollRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2)
    private String stateCode;

    @Column(nullable = false)
    private LocalDate effectiveStart;

    private LocalDate effectiveEnd;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal overtimeRateMultiplier;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal ficaRate;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal futaRate;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal sutaRate;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal federalTaxRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumWage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal standardWeekHours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal excessiveHoursFlag;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal excessiveHoursBlock;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeWarningHours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal aiWarningThreshold;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal overtimeThreshold;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}

