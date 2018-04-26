package com.example.stepheng.eventity.Setup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stepheng.eventity.Classes.WaitlistMember;
import com.example.stepheng.eventity.MainActivity;
import com.example.stepheng.eventity.R;
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

    //declaring UI element variables
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
    private String teamId;

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
                            teamId = teamResults.get(0).toString();

                            DocumentReference joinWaitlist = mFStore.collection("Teams/"+teamId+"/Waitlist").document(user_id);

                            WaitlistMember newWaitListMember = new WaitlistMember(display_name, user_id, "member");
                            //put user in the waitlist
                            joinWaitlist.set(newWaitListMember);
                            //updating user profile with pending membership for new team
                            DocumentReference updateMemberships = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
                            Map<String, Object> membershipData = new HashMap<>();
                            membershipData.put("teamID", teamId);
                            membershipData.put("role", "pending");
                            membershipData.put("name", display_name);
                            membershipData.put("teamName",team_name);
                            updateMemberships.set(membershipData);

                            //show success message and send to Main Activity
                            Toast.makeText(JoinTeamActivity.this, "Team Request sent", Toast.LENGTH_LONG).show();
                            sendtoMain();

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());

                        }
                    }
                });

            }
        });


    }

    private void sendtoMain() {
        Intent mainIntent = new Intent(JoinTeamActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
