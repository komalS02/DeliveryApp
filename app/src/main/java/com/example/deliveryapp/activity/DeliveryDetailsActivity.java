package com.example.deliveryapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deliveryapp.R;
import com.example.deliveryapp.static_class.SelectedOrderDetails;
import com.example.deliveryapp.roomDatabase.AppDatabase;
import com.example.deliveryapp.roomDatabase.DeliveryDetails;
import com.example.deliveryapp.roomDatabase.DeliveryDetailsDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeliveryDetailsActivity extends AppCompatActivity {
    private TextView customerNameTextView, orderNoTextView, addressTextView, deliveryChargeTextView;
    private Button clickButton, retakeButton, submitButton;
    private RadioGroup statusRadioGroup;
    private RadioButton goodRadioButton, damagedRadioButton;
    private EditText damageDetailsEditText, collectedAmountEditText;
    private ImageView ivClickedImage;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private String photoPath;
    private String damageDetails;
    private static final int pic_id = 123;
    private boolean isDamaged;
    private double collectedAmount;
    private ExecutorService executorService;
    private Handler mainHandler;
    private AppDatabase appDatabase;
    private DeliveryDetailsDao deliveryDetailsDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_details);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        appDatabase = AppDatabase.getDatabase(this);
        deliveryDetailsDao = appDatabase.deliveryDetailDao();

        customerNameTextView = findViewById(R.id.customerNameTextView);
        orderNoTextView = findViewById(R.id.orderNoTextView);
        addressTextView = findViewById(R.id.addressTextView);
        deliveryChargeTextView = findViewById(R.id.deliveryChargeTextView);
        ivClickedImage = findViewById(R.id.ivClickedImage);
        clickButton = findViewById(R.id.clickButton);
        retakeButton = findViewById(R.id.retakeButton);
        submitButton = findViewById(R.id.submitButton);
        statusRadioGroup = findViewById(R.id.statusRadioGroup);
        goodRadioButton = findViewById(R.id.goodRadioButton);
        damagedRadioButton = findViewById(R.id.damagedRadioButton);
        damageDetailsEditText = findViewById(R.id.damageDetailsEditText);
        collectedAmountEditText = findViewById(R.id.collectedAmountEditText);


        customerNameTextView.setText("Customer Name: " + SelectedOrderDetails.getSelectedOrderDetails().getCustomerName());
        orderNoTextView.setText("Order No: " + SelectedOrderDetails.getSelectedOrderDetails().getOrderNo());
        addressTextView.setText("Address: " + SelectedOrderDetails.getSelectedOrderDetails().getAddress());
        deliveryChargeTextView.setText("Delivery Charge: " + SelectedOrderDetails.getSelectedOrderDetails().getDeliveryCost());

        clickButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(DeliveryDetailsActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeliveryDetailsActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                startCamera();
            }
        });

        retakeButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(DeliveryDetailsActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeliveryDetailsActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                startCamera();
            }
        });

        statusRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.damagedRadioButton) {
                damageDetailsEditText.setVisibility(View.VISIBLE);
                isDamaged = true;
            } else {
                damageDetailsEditText.setVisibility(View.GONE);
                isDamaged = false;
            }
        });

        submitButton.setOnClickListener(v -> submitDeliveryDetails());
    }

    private void startCamera() {
        Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(camera_intent, pic_id);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ivClickedImage.setImageBitmap(photo);

            if (photo != null) {
                try {
                    File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "delivery_photo.jpg");
                    FileOutputStream fos = new FileOutputStream(photoFile);

                    // Compress bitmap to JPEG format and write to FileOutputStream
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();

                    photoPath = photoFile.getAbsolutePath();
                    Toast.makeText(DeliveryDetailsActivity.this, "Photo captured successfully", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(DeliveryDetailsActivity.this, "Failed to capture photo", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DeliveryDetailsActivity.this, "No image data found", Toast.LENGTH_SHORT).show();
            }

        }
    }


    private void submitDeliveryDetails() {
        double deliveryCost = Double.parseDouble(SelectedOrderDetails.getSelectedOrderDetails().getDeliveryCost());

        try {
            collectedAmount = Double.parseDouble(collectedAmountEditText.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (damagedRadioButton.isChecked()) {
            damageDetails = damageDetailsEditText.getText().toString();
            if (TextUtils.isEmpty(damageDetails)) {
                Toast.makeText(this, "Please enter damage details", Toast.LENGTH_SHORT).show();
                return;
            }
            if (collectedAmount > deliveryCost) {
                Toast.makeText(this, "Collected amount cannot exceed delivery charge", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (collectedAmount != deliveryCost) {
                Toast.makeText(this, "Collected amount must be equal to the delivery charge", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Submit the delivery details (e.g., send to server)
        Toast.makeText(this, "Delivery details submitted", Toast.LENGTH_SHORT).show();
        saveDataIntoDatabase();
    }

    private void saveDataIntoDatabase() {
        // Save the delivery details to the Room database
        DeliveryDetails deliveryDetail = new DeliveryDetails();
        deliveryDetail.orderId = SelectedOrderDetails.getSelectedOrderDetails().getOrderId();
        deliveryDetail.customerName = SelectedOrderDetails.getSelectedOrderDetails().getCustomerName();
        deliveryDetail.orderNo = SelectedOrderDetails.getSelectedOrderDetails().getOrderNo();
        deliveryDetail.address = SelectedOrderDetails.getSelectedOrderDetails().getAddress();
        deliveryDetail.deliveryCost = SelectedOrderDetails.getSelectedOrderDetails().getDeliveryCost();
        deliveryDetail.photoPath = photoPath;
        deliveryDetail.isDamaged = isDamaged;
        deliveryDetail.damageDetails = damageDetails;
        deliveryDetail.collectedAmount = collectedAmount;
        deliveryDetail.status = "Completed";
        deliveryDetail.isCompleted = true;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            db.deliveryDetailDao().insert(deliveryDetail);
            runOnUiThread(() -> {
                Toast.makeText(DeliveryDetailsActivity.this, "Delivery details submitted", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DeliveryDetailsActivity.this, OrderListActivity.class);
                startActivity(intent);
            });
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DeliveryDetailsActivity.this, OrderListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchAndDisplayDeliveryDetails(String orderId) {
        new Thread(() -> {
            DeliveryDetails deliveryDetail = deliveryDetailsDao.getDeliveryDetailByOrderId(orderId);
            if (deliveryDetail != null) {
                runOnUiThread(() -> displayDeliveryDetails(deliveryDetail));
            }
        }).start();
    }

    private void displayDeliveryDetails(DeliveryDetails deliveryDetails){
        customerNameTextView.setText("Customer Name: " + deliveryDetails.customerName);
        orderNoTextView.setText("Order No: " + deliveryDetails.orderNo);
        addressTextView.setText("Address: " + deliveryDetails.address);
        deliveryChargeTextView.setText("Delivery Charge: " + deliveryDetails.deliveryCost);
        collectedAmountEditText.setText(""+deliveryDetails.collectedAmount);
    }
}