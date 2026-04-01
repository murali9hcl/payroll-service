package com.payroll.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollResult {

    private BigDecimal grossPay;

    private BigDecimal fica;

    private BigDecimal futa;

    private BigDecimal suta;

    private BigDecimal federalTax;

    private BigDecimal totalDeductions;

    private BigDecimal netPay;

    private boolean minimumWageValid;


}
