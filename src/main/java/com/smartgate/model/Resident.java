package com.smartgate.model;

public class Resident {
    private int id;
    private String blockNo;
    private String apartmentNo;
    private String fullName;
    private String phone;
    private String rfidId;

    public Resident() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBlockNo() { return blockNo; }
    public void setBlockNo(String blockNo) { this.blockNo = blockNo; }

    public String getApartmentNo() { return apartmentNo; }
    public void setApartmentNo(String apartmentNo) { this.apartmentNo = apartmentNo; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRfidId() { return rfidId; }
    public void setRfidId(String rfidId) { this.rfidId = rfidId; }
}