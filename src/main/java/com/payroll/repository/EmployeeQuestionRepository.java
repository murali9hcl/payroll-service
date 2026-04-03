package com.payroll.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payroll.model.EmployeeQuestion;

public interface EmployeeQuestionRepository extends JpaRepository<EmployeeQuestion, Long> {

    List<EmployeeQuestion> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
}

