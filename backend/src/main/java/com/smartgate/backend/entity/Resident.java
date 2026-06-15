package com.smartgate.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "residents")
public class Resident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apartment_id")
    private Long apartmentId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(length = 30)
    private String phone;

    @Column(name = "rfid_id", length = 80)
    private String rfidId;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public Long getId() {
        return id;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(Long apartmentId) {
        this.apartmentId = apartmentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRfidId() {
        return rfidId;
    }

    public void setRfidId(String rfidId) {
        this.rfidId = rfidId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}