package com.smartgate.backend.dto;

public record ResidentRequest(
        String fullName,
        String phone,
        String rfidId,
        Long apartmentId,
        Boolean active
) {}