package com.rupeedesk7.smsapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    TextView tvWelcome, tvBalance, tvDaily;
    Button btnStartSms, btnSignOut;
    FirebaseFirestore db;
    String phone;
    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvBalance = findViewById(R.id.tvBalance);
        tvDaily = findViewById(R.id.tvDaily);
        btnStartSms = findViewById(R.id.btnStartSms);
        btnSignOut = findViewById(R.id.btnSignOut);

        db = FirebaseFirestore.getInstance();
        SharedPreferences prefs = getSharedPreferences("rupeedesk7_prefs", MODE_PRIVATE);
        phone = prefs.getString("phone", null);
        String name = prefs.getString("name", "");

        if (phone == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvWelcome.setText("Welcome, " + (name.isEmpty()? phone : name));

        // Load user info
        DocumentReference doc = db.collection("users").document(phone);
        doc.addSnapshotListener((snapshot, error) -> {
            if (error != null) return;
            if (snapshot != null && snapshot.exists()) {
                Double balance = snapshot.getDouble("balance"); if (balance == null) balance = 0.0;
                Long dailySent = snapshot.getLong("dailySent"); if (dailySent == null) dailySent = 0L;
                tvBalance.setText(String.format("Balance: ₹%.2f", balance));
                tvDaily.setText("Sent today: " + dailySent);
            }
        });

        btnStartSms.setOnClickListener(v -> ensureSmsPermissionAndSend());

        btnSignOut.setOnClickListener(v -> {
            prefs.edit().remove("phone").remove("name").apply();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void ensureSmsPermissionAndSend() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            scheduleSmsWorker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleSmsWorker();
            } else {
                Toast.makeText(this, "SMS permission required to send messages", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scheduleSmsWorker() {
        OneTimeWorkRequest w = new OneTimeWorkRequest.Builder(SmsWorker.class).build();
        WorkManager.getInstance(this).enqueue(w);
        Toast.makeText(this, "SMS sending scheduled (1 SMS)", Toast.LENGTH_SHORT).show();
    }
}
