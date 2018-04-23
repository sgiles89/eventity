package com.example.stepheng.eventity;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseService extends IntentService {

    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;
    private String user_id;

    @Override
    public void onCreate() {
        super.onCreate();
        mFStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();



    }
    public DatabaseService() {
        super("test-service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // fetch data to save from intent

        // save
        DocumentReference notifyRef = mFStore.collection("Users/"+user_id+"/Notifications").document();
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Myles");
        data.put("country", "To GO");
        notifyRef.set(data);

    }
}
