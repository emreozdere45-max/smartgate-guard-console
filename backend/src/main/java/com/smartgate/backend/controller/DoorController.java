package com.smartgate.backend.controller;

import com.smartgate.backend.dto.DoorUnlockRequest;
import com.smartgate.backend.dto.DoorUnlockResponse;
import com.smartgate.backend.service.DoorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/door")
public class DoorController {
    private final DoorService doorService;

    public DoorController(DoorService doorService) {
        this.doorService = doorService;
    }

    @PostMapping("/unlock")
    public DoorUnlockResponse unlock(@Valid @RequestBody(required = false) DoorUnlockRequest request) {
        DoorUnlockRequest safeRequest = request == null ? new DoorUnlockRequest(null, 1, "main") : request;
        return doorService.unlock(safeRequest);
    }
}
