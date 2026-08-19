package com.nomnom.linguacore.repository;

import com.nomnom.linguacore.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
}
