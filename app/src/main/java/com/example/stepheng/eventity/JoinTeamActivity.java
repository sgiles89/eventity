package com.example.stepheng.eventity;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoinTeamActivity extends AppCompatActivity {

    //declaring
    private EditText teamName;
    private EditText teamCode;
    private Button joinBtn;
    private ProgressBar joinProgress;

    //declaring Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore mFStore;

    private static final String TAG = "JoinTeamActivity";

    private String user_id;
    private String display_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_team);

        //set toolbar
        android.support.v7.widget.Toolbar joinTeamToolbar = findViewById(R.id.join_team_toolbar);
        setSupportActionBar(joinTeamToolbar);
        getSupportActionBar().setTitle("Join a Team");

        //get instances of Firebase auth and firestore
        firebaseAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();

        //assign UI elements
        teamName = findViewById(R.id.join_team_name);
        joinBtn = findViewById(R.id.join_team_btn);
        teamCode = findViewById(R.id.join_team_code);
        joinProgress = findViewById(R.id.join_team_progress);

        user_id = firebaseAuth.getCurrentUser().getUid();

        //retrieving the user's display name from their user profile
        DocumentReference getDisplayName = mFStore.collection("Users").document(user_id);
        getDisplayName.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    display_name = document.getString("name");
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: " + task.getResult().getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        //login for the button
        joinBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final String team_name = teamName.getText().toString();
                final String access_code = teamCode.getText().toString();

                CollectionReference teamsRef = mFStore.collection("Teams");
                Query query = teamsRef.whereEqualTo("accessCode", access_code).whereEqualTo("name", team_name);
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> teamResults = new ArrayList<>();
                            for(DocumentSnapshot document : task.getResult()){
                                teamResults.add(document.getId());
                            }
                            String teamId = teamResults.get(0).toString();

                            DocumentReference joinWaitlist = mFStore.collection("Teams/"+teamId+"/Waitlist").document(user_id);
                            //create Member data hashmap
                            Map<String, Object> waitlistData = new HashMap<>();
                            waitlistData.put("name", display_name);
                            waitlistData.put("role", "user");
                            waitlistData.put("userID", user_id);
                            //add user as the owner
                            joinWaitlist.set(waitlistData);

                            //updating user profile with pending membership for new team
                            DocumentReference updateMemberships = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
                            Map<String, Object> membershipData = new HashMap<>();
                            membershipData.put("teamID", teamId);
                            membershipData.put("role", "pending");

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());

                        }
                    }
                });
            }
        });


    }
}
