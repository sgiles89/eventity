package com.example.stepheng.eventity;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
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
        super("db-service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        // fetch data to save from intent
        String messageType = intent.getStringExtra("message_type");
        String title = intent.getStringExtra("title");
        String message;
        switch (messageType){
            case "team_rejection":
                message = "Your request to join a team has been rejected by an Admin";
                break;
            case "team_accept":
                message = "Your request to join a team has been approved";
                break;
            case "team_remove":
                message = "You have been removed from the team.";
                break;
            default:
                message = "failed to get notification message";
                break;
        }
        // save
        DocumentReference notifyRef = mFStore.collection("Users/"+user_id+"/Notifications").document();
        Notification newNotification = new Notification(null, title, message,messageType);
        notifyRef.set(newNotification);

    }
}
