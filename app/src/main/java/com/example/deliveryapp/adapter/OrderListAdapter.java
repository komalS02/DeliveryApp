package com.example.deliveryapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deliveryapp.R;
import com.example.deliveryapp.static_class.SelectedOrderDetails;
import com.example.deliveryapp.activity.OrderListActivity;
import com.example.deliveryapp.model.response.Order;
import com.example.deliveryapp.roomDatabase.DeliveryDetailsDao;

import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.ViewHolder> {
    private Context context;
    private List<Order> orderList;
    private DeliveryDetailsDao deliveryDetailDao;

    @NonNull
    @Override
    public OrderListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_list_adapter_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderListAdapter.ViewHolder holder, int position) {
        Order currentOrder = orderList.get(position);
        holder.nameTextView.setText(currentOrder.getCustomerName());
        holder.orderNoTextView.setText(currentOrder.getOrderNo());
        holder.addressTextView.setText(currentOrder.getAddress());

        holder.itemView.setOnClickListener(v -> {
            ((OrderListActivity) context).checkLocationAndNavigate(currentOrder);
            SelectedOrderDetails.setSelectedOrderId(currentOrder.getOrderId());
        });

        new Thread(() -> {
            String status = deliveryDetailDao.getStatusByOrderId(currentOrder.getOrderId());
            if (status == null) {
                status = "Pending";
            } else {
                status = "Completed";
            }
            final String finalStatus = status;
            holder.itemView.post(() -> {
                holder.tvStatus.setText("Status :" + finalStatus);
                if ("Completed".equalsIgnoreCase(finalStatus)) {
                    holder.tvStatus.setTextColor(Color.GREEN);
                } else {
                    holder.tvStatus.setTextColor(Color.RED);
                }
            });
        }).start();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public OrderListAdapter(List<Order> orderList, Context context, DeliveryDetailsDao deliveryDetailDao) {
        this.orderList = orderList;
        this.context = context;
        this.deliveryDetailDao = deliveryDetailDao;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView orderNoTextView;
        public TextView addressTextView;
        public TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvCustomerName);
            orderNoTextView = itemView.findViewById(R.id.tvOrderId);
            addressTextView = itemView.findViewById(R.id.tvAddress);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
