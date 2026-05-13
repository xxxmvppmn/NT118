package com.example.waviapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.waviapp.R;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.managers.UserSessionManager;
import com.example.waviapp.models.TaiKhoan;
import com.google.android.material.button.MaterialButton;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends BaseActivity {

    private TextView txtPackageType, txtOriginalPrice, txtDiscountPercent, txtDiscountAmount, txtTotalAmount;
    private RadioGroup radioGroupPayment;
    private MaterialButton btnConfirm;
    private ImageButton btnBack;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        dbHelper = new DatabaseHelper();
        initializeViews();
        receiveDataFromIntent();
        setupListeners();
    }

    private void initializeViews() {
        txtPackageType = findViewById(R.id.txtPackageType);
        txtOriginalPrice = findViewById(R.id.txtOriginalPrice);
        txtDiscountPercent = findViewById(R.id.txtDiscountPercent);
        txtDiscountAmount = findViewById(R.id.txtDiscountAmount);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);
    }

    private void receiveDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            String packageName = intent.getStringExtra("packageName");
            String originalPrice = intent.getStringExtra("originalPrice");
            int discountPercent = intent.getIntExtra("discountPercent", 0);
            String discountAmount = intent.getStringExtra("discountAmount");
            String totalPrice = intent.getStringExtra("totalPrice");

            if (packageName != null) txtPackageType.setText(packageName);
            if (originalPrice != null) txtOriginalPrice.setText(originalPrice + " đ");
            if (discountPercent > 0) txtDiscountPercent.setText("- " + discountPercent + "%");
            if (discountAmount != null) txtDiscountAmount.setText("- " + discountAmount + " đ");
            if (totalPrice != null) txtTotalAmount.setText(totalPrice + " đ");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnConfirm.setOnClickListener(v -> {
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isMomo = (selectedId == R.id.rbMomo);
            showQrDialog(isMomo);
        });
    }

    private void showQrDialog(boolean isMomo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_qr_payment, null);
        builder.setView(dialogView);

        ImageView ivQrCode = dialogView.findViewById(R.id.ivQrCode);
        TextView tvDialogAmount = dialogView.findViewById(R.id.tvDialogAmount);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);

        String totalAmount = txtTotalAmount.getText().toString();
        tvDialogAmount.setText("Số tiền: " + totalAmount);

        if (isMomo) {
            tvDialogTitle.setText("Thanh toán qua Ví MoMo");
            ivQrCode.setImageResource(R.drawable.qr_momo);
            builder.setPositiveButton("Tôi đã thanh toán qua MoMo", (dialog, which) -> simulateVerification());
        } else {
            tvDialogTitle.setText("Chuyển khoản Ngân hàng");
            ivQrCode.setImageResource(R.drawable.qr_bank);
            builder.setPositiveButton("Tôi đã chuyển khoản ngân hàng", (dialog, which) -> simulateVerification());
        }

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void simulateVerification() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang kiểm tra giao dịch, vui lòng đợi...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Thanh toán thành công! Wavi Premium đã được kích hoạt", Toast.LENGTH_LONG).show();
            
            // Cập nhật trạng thái Premium
            savePremiumStatus();
            
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            
        }, 3000);
    }

    private void savePremiumStatus() {
        // 1. Lưu local (SharedPreferences)
        SharedPreferences sharedPref = getSharedPreferences("WaviAppPrefs", Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean("isPremium", true).apply();
        
        // 2. Cập nhật UserSessionManager (để UI đổi ngay lập tức)
        TaiKhoan user = UserSessionManager.getInstance().getUserData();
        if (user != null) {
            user.setPremium(true);
            UserSessionManager.getInstance().updateUserDataLocally(user);
            
            // 3. Cập nhật Firestore (Lưu vĩnh viễn)
            Map<String, Object> updates = new HashMap<>();
            updates.put("premium", true);
            dbHelper.updateUser(user.getId(), updates, null);
        }
    }
}
