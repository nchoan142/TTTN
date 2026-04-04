package com.conghoan.sportbooking.model;

public class BookingItem {
    private long id;
    private String venueName;
    private String courtName;
    private String date;
    private String startTime;
    private String endTime;
    private String status;
    private double totalPrice;

    public BookingItem() {}

    public BookingItem(long id, String venueName, String courtName, String date,
                       String startTime, String endTime, String status, double totalPrice) {
        this.id = id;
        this.venueName = venueName;
        this.courtName = courtName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getVenueName() { return venueName; }
    public void setVenueName(String venueName) { this.venueName = venueName; }

    public String getCourtName() { return courtName; }
    public void setCourtName(String courtName) { this.courtName = courtName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}
