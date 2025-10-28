package com.rupeedesk7.smsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SmsWorker extends Worker {

    FirebaseFirestore db;
    Context ctx;
    private static final String TAG = "SmsWorker";

    public SmsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.ctx = context;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("rupeedesk7_prefs", Context.MODE_PRIVATE);
            String phone = prefs.getString("phone", null);
            if (phone == null) return Result.failure();

            // 1. Query one unsent inventory document
            Query q = db.collection("inventory").whereEqualTo("sent", false).limit(1);
            com.google.android.gms.tasks.Task<QuerySnapshot> task = q.get();
            Tasks.await(task, 30, TimeUnit.SECONDS);
            if (!task.isSuccessful()) {
                Log.e(TAG, "Inventory query failed", task.getException());
                return Result.retry();
            }
            QuerySnapshot snap = task.getResult();
            if (snap == null || snap.size() == 0) {
                Log.i(TAG, "No inventory available");
                return Result.failure();
            }

            DocumentSnapshot invDoc = snap.getDocuments().get(0);
            String targetNumber = invDoc.getString("target");
            String message = invDoc.getString("message");
            Double price = invDoc.getDouble("price"); if (price == null) price = 0.20;

            // 2. send SMS (device must have SIM and permission granted)
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(targetNumber, null, message, null, null);

            // 3. mark inventory as sent
            invDoc.getReference().update("sent", true);

            // 4. update user balance & dailySent atomically
            DocumentReference userDoc = db.collection("users").document(phone);
            Map<String, Object> updates = new HashMap<>();
            updates.put("balance", FieldValue.increment(price));
            updates.put("dailySent", FieldValue.increment(1));
            userDoc.update(updates);

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
