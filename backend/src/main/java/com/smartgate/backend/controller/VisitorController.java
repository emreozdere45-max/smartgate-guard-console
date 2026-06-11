package com.smartgate.backend.controller;

import com.smartgate.backend.dto.VisitorRequest;
import com.smartgate.backend.dto.VisitorResponse;
import com.smartgate.backend.service.VisitorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {
    private final VisitorService visitorService;

    public VisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping
    public List<VisitorResponse> list() {
        return visitorService.findRecent();
    }

    @PostMapping
    public VisitorResponse create(@Valid @RequestBody VisitorRequest request) {
        return visitorService.create(request);
    }

    @PutMapping("/{id}/approve")
    public VisitorResponse approve(@PathVariable Long id) {
        return visitorService.approve(id);
    }

    @PutMapping("/{id}/reject")
    public VisitorResponse reject(@PathVariable Long id) {
        return visitorService.reject(id);
    }

    @PutMapping("/{id}/exit")
    public VisitorResponse exit(@PathVariable Long id) {
        return visitorService.exit(id);
    }
}