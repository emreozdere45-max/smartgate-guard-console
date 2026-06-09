package com.smartgate.backend.repository;

import com.smartgate.backend.entity.IntercomDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntercomDeviceRepository extends JpaRepository<IntercomDevice, Long> {
    List<IntercomDevice> findByActiveTrueOrderByNameAsc();

    Optional<IntercomDevice> findFirstByActiveTrueOrderByIdAsc();
}

