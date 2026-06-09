package com.smartgate.backend.repository;

import com.smartgate.backend.entity.GateLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GateLogRepository extends JpaRepository<GateLog, Long> {
    List<GateLog> findByOrderByEventTimeDesc(Pageable pageable);
}

