package com.smartgate.backend.service;

import com.smartgate.backend.dto.HealthResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class HealthService {
    private final JdbcTemplate jdbcTemplate;

    public HealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HealthResponse check() {
        String database = "CONNECTED";
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        } catch (Exception e) {
            database = "DISCONNECTED";
        }

        String status = "CONNECTED".equals(database) ? "UP" : "DEGRADED";
        return new HealthResponse(status, database, Instant.now());
    }
}

