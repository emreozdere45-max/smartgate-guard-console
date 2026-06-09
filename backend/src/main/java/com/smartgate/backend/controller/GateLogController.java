package com.smartgate.backend.controller;

import com.smartgate.backend.dto.GateLogResponse;
import com.smartgate.backend.service.GateLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gate-logs")
public class GateLogController {
    private final GateLogService gateLogService;

    public GateLogController(GateLogService gateLogService) {
        this.gateLogService = gateLogService;
    }

    @GetMapping
    public List<GateLogResponse> list(@RequestParam(defaultValue = "50") int limit) {
        return gateLogService.findRecent(limit);
    }
}

