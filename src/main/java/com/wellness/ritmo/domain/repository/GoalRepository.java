package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.Goal;
import com.wellness.ritmo.domain.model.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    Optional<Goal> findByIdAndUserIdAndStatus(Long id, Long userId, GoalStatus status);

    @Modifying
    @Query("UPDATE Goal g SET g.status = :status, g.updatedAt = CURRENT_TIMESTAMP WHERE g.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") GoalStatus status);
}
