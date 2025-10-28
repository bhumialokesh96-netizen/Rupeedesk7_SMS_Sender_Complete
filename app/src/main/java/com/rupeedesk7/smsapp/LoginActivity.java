package com.rupeedesk7.smsapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText etPhone, etName;
    Button btnLogin;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        etName = findViewById(R.id.etName);
        btnLogin = findViewById(R.id.btnLogin);
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, "Enter phone", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save locally
            SharedPreferences prefs = getSharedPreferences("rupeedesk7_prefs", MODE_PRIVATE);
            prefs.edit().putString("phone", phone).putString("name", name).apply();

            // Create or update Firestore user doc (collection: users, id = phone)
            Map<String, Object> user = new HashMap<>();
            user.put("phone", phone);
            user.put("name", name);
            user.put("balance", 0.0);
            user.put("dailySent", 0);
            user.put("dailyLimit", 50);
            user.put("blocked", false);

            DocumentReference doc = db.collection("users").document(phone);
            doc.get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) {
                    doc.set(user).addOnSuccessListener(aVoid -> {
                        startDashboard();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                } else {
                    // already exists — update name if provided
                    if (!name.isEmpty()) {
                        doc.update("name", name);
                    }
                    startDashboard();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        });
    }

    private void startDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
