package com.smartgate.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VisitorRequest(
        @NotBlank @Size(max = 120) String visitorName,
        @NotBlank @Size(max = 40) String visitorType,
        @Size(max = 20) String blockName,
        @Size(max = 20) String apartmentNo,
        String visitReason
) {
}