package com.payroll.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "payroll.rules")
public class PayrollRulesProperties {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal overtimeRateMultiplier = new BigDecimal("1.5");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal ficaRate = new BigDecimal("0.0765");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal futaRate = new BigDecimal("0.006");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal sutaRate = new BigDecimal("0.027");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal federalTaxRate = new BigDecimal("0.10");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal minimumWage = new BigDecimal("14.00");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal standardWeekHours = new BigDecimal("40.00");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal excessiveHoursFlag = new BigDecimal("45.00");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal excessiveHoursBlock = new BigDecimal("60.00");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal overtimeWarningHours = new BigDecimal("15.00");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal aiWarningThreshold = new BigDecimal("32.00");

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal overtimeThreshold = new BigDecimal("40.00");

    public BigDecimal getOvertimeRateMultiplier() {
        return overtimeRateMultiplier;
    }

    public void setOvertimeRateMultiplier(BigDecimal overtimeRateMultiplier) {
        this.overtimeRateMultiplier = overtimeRateMultiplier;
    }

    public BigDecimal getFicaRate() {
        return ficaRate;
    }

    public void setFicaRate(BigDecimal ficaRate) {
        this.ficaRate = ficaRate;
    }

    public BigDecimal getFutaRate() {
        return futaRate;
    }

    public void setFutaRate(BigDecimal futaRate) {
        this.futaRate = futaRate;
    }

    public BigDecimal getSutaRate() {
        return sutaRate;
    }

    public void setSutaRate(BigDecimal sutaRate) {
        this.sutaRate = sutaRate;
    }

    public BigDecimal getFederalTaxRate() {
        return federalTaxRate;
    }

    public void setFederalTaxRate(BigDecimal federalTaxRate) {
        this.federalTaxRate = federalTaxRate;
    }

    public BigDecimal getMinimumWage() {
        return minimumWage;
    }

    public void setMinimumWage(BigDecimal minimumWage) {
        this.minimumWage = minimumWage;
    }

    public BigDecimal getStandardWeekHours() {
        return standardWeekHours;
    }

    public void setStandardWeekHours(BigDecimal standardWeekHours) {
        this.standardWeekHours = standardWeekHours;
    }

    public BigDecimal getExcessiveHoursFlag() {
        return excessiveHoursFlag;
    }

    public void setExcessiveHoursFlag(BigDecimal excessiveHoursFlag) {
        this.excessiveHoursFlag = excessiveHoursFlag;
    }

    public BigDecimal getExcessiveHoursBlock() {
        return excessiveHoursBlock;
    }

    public void setExcessiveHoursBlock(BigDecimal excessiveHoursBlock) {
        this.excessiveHoursBlock = excessiveHoursBlock;
    }

    public BigDecimal getOvertimeWarningHours() {
        return overtimeWarningHours;
    }

    public void setOvertimeWarningHours(BigDecimal overtimeWarningHours) {
        this.overtimeWarningHours = overtimeWarningHours;
    }

    public BigDecimal getAiWarningThreshold() {
        return aiWarningThreshold;
    }

    public void setAiWarningThreshold(BigDecimal aiWarningThreshold) {
        this.aiWarningThreshold = aiWarningThreshold;
    }

    public BigDecimal getOvertimeThreshold() {
        return overtimeThreshold;
    }

    public void setOvertimeThreshold(BigDecimal overtimeThreshold) {
        this.overtimeThreshold = overtimeThreshold;
    }
}

