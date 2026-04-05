package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserIdOrderByStartedAtDesc(Long userId);
}
