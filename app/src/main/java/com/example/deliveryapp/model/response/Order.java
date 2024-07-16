package com.example.deliveryapp.model.response;

import com.google.gson.annotations.SerializedName;

public class Order {
    @SerializedName("order_id")
    private String orderId;

    @SerializedName("order_no")
    private String orderNo;

    @SerializedName("customer_name")
    private String customerName;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("address")
    private String address;

    @SerializedName("delivery_cost")
    private String deliveryCost;

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDeliveryCost() { return deliveryCost; }
    public void setDeliveryCost(String deliveryCost) { this.deliveryCost = deliveryCost; }
}
