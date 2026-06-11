package com.smartgate.model;

public class Visitor {
    private Long id;
    private String visitorName;
    private String visitorType;
    private String blockName;
    private String apartmentNo;
    private String visitReason;
    private String status;
    private String entryTime;
    private String exitTime;

    public Long getId() {
        return id;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public String getVisitorType() {
        return visitorType;
    }

    public String getBlockName() {
        return blockName;
    }

    public String getApartmentNo() {
        return apartmentNo;
    }

    public String getVisitReason() {
        return visitReason;
    }

    public String getStatus() {
        return status;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public String getExitTime() {
        return exitTime;
    }

    public String getApartmentLabel() {
        String block = blockName == null ? "" : blockName;
        String apartment = apartmentNo == null ? "" : apartmentNo;
        return (block + "-" + apartment).replaceAll("^-|-$", "");
    }
}
