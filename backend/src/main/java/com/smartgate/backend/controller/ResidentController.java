package com.smartgate.backend.controller;

import com.smartgate.backend.dto.ResidentRequest;
import com.smartgate.backend.dto.ResidentResponse;
import com.smartgate.backend.dto.RfidUpdateRequest;
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
                .map(this::toResponse)
                .toList();
    }

    @PostMapping
    public ResidentResponse create(@RequestBody ResidentRequest request) {
        Resident resident = new Resident();
        resident.setFullName(request.fullName());
        resident.setPhone(request.phone());
        resident.setRfidId(request.rfidId());
        resident.setApartmentId(request.apartmentId());
        resident.setActive(request.active() == null || request.active());

        return toResponse(residentRepository.save(resident));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        residentRepository.deleteById(id);
    }

    @PutMapping("/{id}/active")
    public ResidentResponse updateActive(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        Resident resident = findById(id);
        resident.setActive(active);

        return toResponse(residentRepository.save(resident));
    }

    @PutMapping("/{id}/rfid")
    public ResidentResponse updateRfid(
            @PathVariable Long id,
            @RequestBody RfidUpdateRequest request
    ) {
        Resident resident = findById(id);
        resident.setRfidId(request.rfidId());

        return toResponse(residentRepository.save(resident));
    }

    private Resident findById(Long id) {
        return residentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found: " + id));
    }

    private ResidentResponse toResponse(Resident resident) {
        return new ResidentResponse(
                resident.getId(),
                resident.getFullName(),
                resident.getPhone(),
                resident.getRfidId(),
                resident.getApartmentId(),
                resident.getActive()
        );
    }
}