package com.example.deliveryapp.retrofit_services;

import com.example.deliveryapp.model.response.OrderResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("orderlist.php") // Replace with your actual endpoint
    Call<OrderResponse> getOrderList();
}
