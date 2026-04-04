package com.conghoan.sportbooking.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Auth
    @POST("auth/login")
    Call<Map<String, Object>> login(@Body Map<String, String> body);

    @POST("auth/register")
    Call<Map<String, Object>> register(@Body Map<String, String> body);

    // Categories
    @GET("categories")
    Call<Map<String, Object>> getCategories();

    // Venues
    @GET("venues")
    Call<Map<String, Object>> getVenues();

    @GET("venues/category/{categoryId}")
    Call<Map<String, Object>> getVenuesByCategory(@Path("categoryId") long categoryId);

    @GET("venues/search")
    Call<Map<String, Object>> searchVenues(@Query("q") String query);

    @GET("venues/{id}")
    Call<Map<String, Object>> getVenueById(@Path("id") long id);

    @GET("venues/{id}/courts")
    Call<Map<String, Object>> getVenueCourts(@Path("id") long id);

    @GET("venues/{id}/slots")
    Call<Map<String, Object>> getVenueSlots(@Path("id") long id, @Query("date") String date);

    // Bookings
    @POST("bookings")
    Call<Map<String, Object>> createBooking(@Body Map<String, Object> body);

    @GET("bookings/my")
    Call<Map<String, Object>> getMyBookings();

    @PUT("bookings/{id}/cancel")
    Call<Map<String, Object>> cancelBooking(@Path("id") long id);

    // Reviews
    @GET("reviews/venue/{venueId}")
    Call<Map<String, Object>> getVenueReviews(@Path("venueId") Long venueId);

    @POST("reviews")
    Call<Map<String, Object>> createReview(@Body Map<String, Object> body);

    // Change password
    @PUT("auth/change-password")
    Call<Map<String, Object>> changePassword(@Body Map<String, String> body);

    // Admin
    @GET("admin/stats")
    Call<Map<String, Object>> getAdminStats();

    @GET("admin/users")
    Call<Map<String, Object>> getAdminUsers();

    @PUT("admin/users/{id}/role")
    Call<Map<String, Object>> updateUserRole(@Path("id") Long id, @Body Map<String, Object> body);

    @GET("admin/venues")
    Call<Map<String, Object>> getAdminVenues();

    @PUT("admin/venues/{id}/toggle")
    Call<Map<String, Object>> toggleVenue(@Path("id") Long id);

    @GET("admin/bookings")
    Call<Map<String, Object>> getAdminBookings();

    @DELETE("admin/users/{id}")
    Call<Map<String, Object>> deleteUser(@Path("id") Long id);

    @GET("admin/categories")
    Call<Map<String, Object>> getAdminCategories();

    @POST("admin/categories")
    Call<Map<String, Object>> createCategory(@Body Map<String, String> body);

    @PUT("admin/categories/{id}")
    Call<Map<String, Object>> updateCategory(@Path("id") Long id, @Body Map<String, String> body);

    @DELETE("admin/categories/{id}")
    Call<Map<String, Object>> deleteCategory(@Path("id") Long id);

    @PUT("admin/bookings/{id}/status")
    Call<Map<String, Object>> updateBookingStatus(@Path("id") Long id, @Body Map<String, Object> body);
}
