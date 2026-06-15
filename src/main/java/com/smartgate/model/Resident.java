package com.smartgate.model;

public class Resident {
    private Long id;
    private String fullName;
    private String phone;
    private String rfidId;
    private Long apartmentId;
    private boolean active;

    public Resident() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRfidId() { return rfidId; }
    public void setRfidId(String rfidId) { this.rfidId = rfidId; }

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}