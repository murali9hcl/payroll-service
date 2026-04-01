package com.payroll.controller;

import com.payroll.model.PayrollInput;
import com.payroll.model.PayrollResult;
import com.payroll.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payroll")
public class PayrollController {

    @Autowired
    PayrollService payrollService;

    @PostMapping("/calculate")
    public PayrollResult calculate(@RequestBody PayrollInput input){
        return payrollService.calculate(input);

    }
}