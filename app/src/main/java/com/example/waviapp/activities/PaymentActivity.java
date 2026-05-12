package com.example.waviapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.waviapp.R;
import com.google.android.material.button.MaterialButton;

public class PaymentActivity extends AppCompatActivity {

    private TextView txtPackageType, txtOriginalPrice, txtDiscountPercent, txtDiscountAmount, txtTotalAmount;
    private RadioGroup radioGroupPayment;
    private MaterialButton btnConfirm, btnRestore;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Khởi tạo các view
        initializeViews();

        // Nhận dữ liệu từ Intent
        receiveDataFromIntent();

        // Setup các listener
        setupListeners();
    }

    /**
     * Khởi tạo các view từ layout
     */
    private void initializeViews() {
        txtPackageType = findViewById(R.id.txtPackageType);
        txtOriginalPrice = findViewById(R.id.txtOriginalPrice);
        txtDiscountPercent = findViewById(R.id.txtDiscountPercent);
        txtDiscountAmount = findViewById(R.id.txtDiscountAmount);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnRestore = findViewById(R.id.btnRestore);
        btnBack = findViewById(R.id.btnBack);
    }

    /**
     * Nhận dữ liệu từ Intent và hiển thị lên các TextView
     */
    private void receiveDataFromIntent() {
        Intent intent = getIntent();
        
        if (intent != null) {
            // Nhận dữ liệu từ PremiumActivity
            String packageName = intent.getStringExtra("packageName");
            String originalPrice = intent.getStringExtra("originalPrice");
            int discountPercent = intent.getIntExtra("discountPercent", 0);
            String discountAmount = intent.getStringExtra("discountAmount");
            String totalPrice = intent.getStringExtra("totalPrice");

            // Hiển thị dữ liệu lên các TextView
            if (packageName != null) {
                txtPackageType.setText(packageName);
            }

            if (originalPrice != null) {
                txtOriginalPrice.setText(originalPrice + " đ");
            }

            if (discountPercent > 0) {
                txtDiscountPercent.setText("- " + discountPercent + "%");
            }

            if (discountAmount != null) {
                txtDiscountAmount.setText("- " + discountAmount);
            }

            if (totalPrice != null) {
                txtTotalAmount.setText(totalPrice + " đ");
            }
        }
    }

    /**
     * Setup các listener cho nút và các view khác
     */
    private void setupListeners() {
        // Xử lý nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý nút xác nhận thanh toán
        btnConfirm.setOnClickListener(v -> handlePaymentConfirmation());

        // Xử lý nút khôi phục giao dịch
        btnRestore.setOnClickListener(v -> handleRestoreTransaction());
    }

    /**
     * Xử lý sự kiện xác nhận thanh toán
     * Kiểm tra RadioButton được chọn (Momo hay Bank) và hiển thị thông báo Toast
     */
    private void handlePaymentConfirmation() {
        int selectedRadioId = radioGroupPayment.getCheckedRadioButtonId();

        if (selectedRadioId == -1) {
            // Không có phương thức thanh toán được chọn
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedRadioId);
        String paymentMethod = selectedRadioButton.getText().toString();

        // Hiển thị thông báo Toast tương ứng với phương thức thanh toán được chọn
        if (selectedRadioId == R.id.rbMomo) {
            Toast.makeText(this, "Thanh toán qua Momo: " + paymentMethod, Toast.LENGTH_SHORT).show();
            // TODO: Implement Momo payment logic here
        } else if (selectedRadioId == R.id.rbBank) {
            Toast.makeText(this, "Thanh toán qua Thẻ ngân hàng: " + paymentMethod, Toast.LENGTH_SHORT).show();
            // TODO: Implement Bank payment logic here
        }
    }

    /**
     * Xử lý sự kiện khôi phục giao dịch
     */
    private void handleRestoreTransaction() {
        Toast.makeText(this, "Đang kiểm tra giao dịch cũ...", Toast.LENGTH_SHORT).show();
        // TODO: Implement restore transaction logic here
    }
}

