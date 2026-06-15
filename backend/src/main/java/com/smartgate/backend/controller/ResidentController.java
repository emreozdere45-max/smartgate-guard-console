package com.smartgate.backend.controller;

import com.smartgate.backend.dto.ResidentResponse;
import com.smartgate.backend.entity.Resident;
import com.smartgate.backend.repository.ResidentRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residents")
public class ResidentController {
    private final ResidentRepository residentRepository;

    public ResidentController(ResidentRepository residentRepository) {
        this.residentRepository = residentRepository;
    }

    @GetMapping
    public List<ResidentResponse> list() {
        return residentRepository.findAll().stream()
                .map(r -> new ResidentResponse(
                        r.getId(),
                        r.getFullName(),
                        r.getPhone(),
                        r.getRfidId(),
                        r.getApartmentId(),
                        r.getActive()
                ))
                .toList();
    }
}