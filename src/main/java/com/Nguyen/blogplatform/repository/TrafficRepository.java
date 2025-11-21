package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Traffic;
import com.Nguyen.blogplatform.model.Traffic.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficRepository extends JpaRepository<Traffic, Long> {

    Optional<Traffic> findByPeriodTypeAndPeriodDate(PeriodType periodType, LocalDate periodDate);

    List<Traffic> findAllByPeriodTypeAndPeriodDateBetweenOrderByPeriodDateAsc(PeriodType periodType, LocalDate start, LocalDate end);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Traffic t set t.accessCount = t.accessCount + :delta where t.periodType = :type and t.periodDate = :date")
    int incrementCounter(@Param("type") PeriodType type, @Param("date") LocalDate date, @Param("delta") long delta);
}
