package com.conghoan.sportbooking.model;

public class SlotInfo {
    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_BOOKED = "BOOKED";
    public static final String STATUS_LOCKED = "LOCKED";
    public static final String STATUS_EVENT = "EVENT";

    private int courtIndex;
    private int timeIndex;
    private String status;
    private boolean selected;

    public SlotInfo() {}

    public SlotInfo(int courtIndex, int timeIndex, String status, boolean selected) {
        this.courtIndex = courtIndex;
        this.timeIndex = timeIndex;
        this.status = status;
        this.selected = selected;
    }

    public int getCourtIndex() { return courtIndex; }
    public void setCourtIndex(int courtIndex) { this.courtIndex = courtIndex; }

    public int getTimeIndex() { return timeIndex; }
    public void setTimeIndex(int timeIndex) { this.timeIndex = timeIndex; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}