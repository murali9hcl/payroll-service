package com.payroll.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.payroll.model.StatePayrollRule;

public interface StatePayrollRuleRepository extends JpaRepository<StatePayrollRule, Long> {

    @Query("""
            select r
            from StatePayrollRule r
            where upper(r.stateCode) = upper(:stateCode)
              and r.effectiveStart <= :asOfDate
              and (r.effectiveEnd is null or r.effectiveEnd >= :asOfDate)
            order by r.effectiveStart desc
            """)
    List<StatePayrollRule> findEffectiveRules(@Param("stateCode") String stateCode, @Param("asOfDate") LocalDate asOfDate);

    @Query("""
            select case when count(r) > 0 then true else false end
            from StatePayrollRule r
            where upper(r.stateCode) = upper(:stateCode)
              and (:excludeId is null or r.id <> :excludeId)
              and r.effectiveStart <= :newEffectiveEnd
              and (r.effectiveEnd is null or r.effectiveEnd >= :newEffectiveStart)
            """)
    boolean hasOverlappingRange(
            @Param("stateCode") String stateCode,
            @Param("newEffectiveStart") LocalDate newEffectiveStart,
            @Param("newEffectiveEnd") LocalDate newEffectiveEnd,
            @Param("excludeId") Long excludeId);

    List<StatePayrollRule> findByStateCodeIgnoreCaseOrderByEffectiveStartDesc(String stateCode);

}

