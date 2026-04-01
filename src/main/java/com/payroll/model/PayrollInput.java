package com.payroll.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PayrollInput {

    private BigDecimal hoursWorked;

    private BigDecimal overtimeHours;

    private BigDecimal hourlyRate;

}
