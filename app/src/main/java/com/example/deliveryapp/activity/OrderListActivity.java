package com.example.deliveryapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deliveryapp.retrofit_services.ApiClient;
import com.example.deliveryapp.retrofit_services.ApiService;
import com.example.deliveryapp.model.response.Order;
import com.example.deliveryapp.adapter.OrderListAdapter;
import com.example.deliveryapp.model.response.OrderResponse;
import com.example.deliveryapp.R;
import com.example.deliveryapp.static_class.SelectedOrderDetails;
import com.example.deliveryapp.roomDatabase.AppDatabase;
import com.example.deliveryapp.roomDatabase.DeliveryDetails;
import com.example.deliveryapp.roomDatabase.DeliveryDetailsDao;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;

public class OrderListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;
    ArrayList<Order> orderArrayList = new ArrayList<>();
    private TextView deliveriesCompletedTextView;
    private TextView cashCollectedTextView;
    private AppDatabase appDatabase;
    private DeliveryDetailsDao deliveryDetailsDao;
    private ExecutorService executorService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        appDatabase = AppDatabase.getDatabase(this);
        deliveryDetailsDao = appDatabase.deliveryDetailDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        deliveriesCompletedTextView = findViewById(R.id.tvDeliveriesCompleted);
        cashCollectedTextView = findViewById(R.id.tvTotalCollected);
        fetchOrderData();


    }

    private void fetchOrderData() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<OrderResponse> call = apiService.getOrderList();

        call.enqueue(new retrofit2.Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, retrofit2.Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orderList = response.body().getOrderList();
                    orderArrayList.addAll(response.body().getOrderList());
                    adapter = new OrderListAdapter(orderList, OrderListActivity.this,deliveryDetailsDao);
                    recyclerView.setAdapter(adapter);
                    updateHighlights();
                } else {
                    Toast.makeText(OrderListActivity.this, "Failed to get data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }


    public void checkLocationAndNavigate(final Order order) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location currentLocation = task.getResult();
                double distance = calculateDistance(currentLocation.getLatitude(), currentLocation.getLongitude(),
                        Double.parseDouble(order.getLatitude()), Double.parseDouble(order.getLongitude()));

                if (distance <= 50) {
                    // Navigate to the delivery page
                    Intent intent = new Intent(OrderListActivity.this, DeliveryDetailsActivity.class);
                    startActivity(intent);
                    SelectedOrderDetails.setSelectedOrderDetails(order);
                    Toast.makeText(OrderListActivity.this, "Navigating to delivery page", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OrderListActivity.this, "Delivery location is too far", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(OrderListActivity.this, "Failed to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return result[0];
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                // You can retry getting the location if needed
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateHighlights() {
        executorService.execute(() -> {
            List<DeliveryDetails> ordersList = appDatabase.deliveryDetailDao().getAllDeliveryDetails();
            int totalOrders = orderArrayList.size();
            final int[] completedOrders = {0};
            final int[] cashCollected = {0};

            mainHandler.post(() -> {

                for (DeliveryDetails orders : ordersList) {
                    if (orders.isCompleted) {
                        completedOrders[0]++;
                        cashCollected[0] += orders.collectedAmount;
                        System.out.println("The pending data :" + orders.isCompleted + orders.collectedAmount);
                    }
                }
                deliveriesCompletedTextView.setText(String.format("Deliveries completed: %d/%d", completedOrders[0], totalOrders));
                cashCollectedTextView.setText(String.format("Cash collected: Rs.%d", cashCollected[0]));
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
    }
}