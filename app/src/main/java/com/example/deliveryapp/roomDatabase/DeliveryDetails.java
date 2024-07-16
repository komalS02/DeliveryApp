package com.example.deliveryapp.roomDatabase;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "delivery_details")
public class DeliveryDetails {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String orderId;
    public String customerName;
    public String orderNo;
    public String address;
    public String deliveryCost;
    public String photoPath;
    public boolean isDamaged;
    public String damageDetails;
    public double collectedAmount;
    public boolean isCompleted;
    public String status;
}
