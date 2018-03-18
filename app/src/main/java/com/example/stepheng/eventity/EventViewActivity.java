package com.example.stepheng.eventity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventViewActivity extends AppCompatActivity {

    //declaring FireBase variables
    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;

    //declaring user,team and event variables
    private String user_id;
    private String team_id;
    private String TAG = "EventViewActivity";

    //declaring and binding UI elements
    @BindView(R.id.event_title_text) TextView event_title;
    @BindView(R.id.event_date_text)TextView event_date;
    @BindView(R.id.event_time_text)TextView event_time;
    @BindView(R.id.event_description_text)TextView event_description;
    @BindView(R.id.event_location_text)TextView event_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        final String event_id = intent.getExtras().getString("event_id");
        //retrieving user's Firebase ID

        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(EventViewActivity.this, LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        //retrieving Team ID
        DocumentReference teamIDRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
        teamIDRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        String team_id = document.getString("teamID");
                        DocumentReference eventInfoRef = mFStore.collection("Teams/"+team_id+"/Events").document(event_id);
                        eventInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Event thisEvent = documentSnapshot.toObject(Event.class);
                                event_title.setText(thisEvent.getTitle());
                                event_date.setText(thisEvent.getNiceDate());
                                event_time.setText(thisEvent.getTime());
                                event_location.setText(thisEvent.getLocation());
                                event_description.setText((thisEvent.getDescription()));

                            }
                        });
                    } else {
                        Log.d(TAG, "profile has no team");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



    }

}
