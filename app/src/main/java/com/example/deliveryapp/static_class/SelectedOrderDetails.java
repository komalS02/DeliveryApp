package com.example.deliveryapp.static_class;

import com.example.deliveryapp.model.response.Order;

public class SelectedOrderDetails {
  private static Order selectedOrderDetails;
  private static String selectedOrderId;


    public static Order getSelectedOrderDetails() {
        return selectedOrderDetails;
    }

    public static void setSelectedOrderDetails(Order selectedOrderDetails) {
        SelectedOrderDetails.selectedOrderDetails = selectedOrderDetails;
    }

    public static String getSelectedOrderId() {
        return selectedOrderId;
    }

    public static void setSelectedOrderId(String selectedOrderId) {
        SelectedOrderDetails.selectedOrderId = selectedOrderId;
    }
}
