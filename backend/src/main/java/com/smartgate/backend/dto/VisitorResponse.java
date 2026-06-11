package com.smartgate.backend.dto;

import java.time.LocalDateTime;

public record VisitorResponse(
        Long id,
        String visitorName,
        String visitorType,
        String blockName,
        String apartmentNo,
        String visitReason,
        String status,
        LocalDateTime entryTime,
        LocalDateTime exitTime
) {}