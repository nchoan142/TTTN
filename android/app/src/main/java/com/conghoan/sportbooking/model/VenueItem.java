package com.conghoan.sportbooking.model;

public class VenueItem {
    private long id;
    private String name;
    private String address;
    private double rating;
    private int ratingCount;
    private String openTime;
    private String closeTime;
    private double pricePerSlot;
    private String phone;
    private String categoryName;
    private String imageUrl;

    public VenueItem() {}

    public VenueItem(long id, String name, String address, double rating, int ratingCount,
                     String openTime, String closeTime, double pricePerSlot, String phone, String categoryName) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.pricePerSlot = pricePerSlot;
        this.phone = phone;
        this.categoryName = categoryName;
    }

    public VenueItem(long id, String name, String address, double rating, int ratingCount,
                     String openTime, String closeTime, double pricePerSlot, String phone,
                     String categoryName, String imageUrl) {
        this(id, name, address, rating, ratingCount, openTime, closeTime, pricePerSlot, phone, categoryName);
        this.imageUrl = imageUrl;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }

    public String getCloseTime() { return closeTime; }
    public void setCloseTime(String closeTime) { this.closeTime = closeTime; }

    public double getPricePerSlot() { return pricePerSlot; }
    public void setPricePerSlot(double pricePerSlot) { this.pricePerSlot = pricePerSlot; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
