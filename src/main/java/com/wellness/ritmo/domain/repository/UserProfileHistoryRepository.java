package com.wellness.ritmo.domain.repository;

import com.wellness.ritmo.domain.model.UserProfileHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProfileHistoryRepository extends JpaRepository<UserProfileHistory, Long> {
    List<UserProfileHistory> findTop3ByUserIdOrderByRecordedAtDesc(Long userId);
}
