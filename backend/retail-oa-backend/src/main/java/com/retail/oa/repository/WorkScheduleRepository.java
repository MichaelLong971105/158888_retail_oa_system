package com.retail.oa.repository;

import com.retail.oa.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for employee weekly schedules.
 */
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    Optional<WorkSchedule> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    List<WorkSchedule> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAsc(Long employeeId, LocalDate startDate, LocalDate endDate);
}
