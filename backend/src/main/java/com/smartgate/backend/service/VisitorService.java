package com.smartgate.backend.service;

import com.smartgate.backend.dto.VisitorRequest;
import com.smartgate.backend.dto.VisitorResponse;
import com.smartgate.backend.entity.Visitor;
import com.smartgate.backend.repository.VisitorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitorService {
    private final VisitorRepository visitorRepository;

    public VisitorService(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    public List<VisitorResponse> findRecent() {
        return visitorRepository.findTop50ByOrderByEntryTimeDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public VisitorResponse create(VisitorRequest request) {
        Visitor visitor = new Visitor();
        visitor.setVisitorName(request.visitorName());
        visitor.setVisitorType(request.visitorType());
        visitor.setBlockName(request.blockName());
        visitor.setApartmentNo(request.apartmentNo());
        visitor.setVisitReason(request.visitReason());
        visitor.setStatus("PENDING");

        return toResponse(visitorRepository.save(visitor));
    }

    public VisitorResponse approve(Long id) {
        Visitor visitor = findById(id);
        visitor.setStatus("APPROVED");
        return toResponse(visitorRepository.save(visitor));
    }

    public VisitorResponse reject(Long id) {
        Visitor visitor = findById(id);
        visitor.setStatus("REJECTED");
        return toResponse(visitorRepository.save(visitor));
    }

    public VisitorResponse exit(Long id) {
        Visitor visitor = findById(id);
        visitor.setStatus("EXITED");
        visitor.setExitTime(LocalDateTime.now());
        return toResponse(visitorRepository.save(visitor));
    }

    private Visitor findById(Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visitor not found: " + id));
    }

    private VisitorResponse toResponse(Visitor visitor) {
        return new VisitorResponse(
                visitor.getId(),
                visitor.getVisitorName(),
                visitor.getVisitorType(),
                visitor.getBlockName(),
                visitor.getApartmentNo(),
                visitor.getVisitReason(),
                visitor.getStatus(),
                visitor.getEntryTime(),
                visitor.getExitTime()
        );
    }
}