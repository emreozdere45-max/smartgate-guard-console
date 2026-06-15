package com.smartgate.backend.dto;

public record ResidentResponse(
        Long id,
        String fullName,
        String phone,
        String rfidId,
        Long apartmentId,
        Boolean active
) {}