package com.smartgate.backend.dto;

public record VisitorRequest(
        String visitorName,
        String visitorType,
        String blockName,
        String apartmentNo,
        String visitReason
) {}
