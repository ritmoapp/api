package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.UserAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAvailabilityRepository extends JpaRepository<UserAvailability, Long> {

    @Query("SELECT ua FROM UserAvailability ua WHERE ua.user.id = :userId AND (ua.validUntil IS NULL OR ua.validUntil >= CURRENT_DATE)")
    List<UserAvailability> findValidByUserId(@Param("userId") Long userId);
}
