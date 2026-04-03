package com.payroll.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class PayrollInput {

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal hoursWorked;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal overtimeHours;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal hourlyRate;

}
