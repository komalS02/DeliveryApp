package com.example.deliveryapp.roomDatabase;

import androidx.room.Dao;
import androidx.room.Insert;

import java.util.List;

import androidx.room.Query;

@Dao
public interface DeliveryDetailsDao {
    @Insert
    void insert(DeliveryDetails deliveryDetail);

    @Query("SELECT * FROM delivery_details")
    List<DeliveryDetails> getAllDeliveryDetails();

    @Query("SELECT status FROM delivery_details WHERE orderId = :orderId")
    String getStatusByOrderId(String orderId);

    @Query("SELECT * FROM delivery_details WHERE orderId = :orderId LIMIT 1")
    DeliveryDetails getDeliveryDetailByOrderId(String orderId);
}
