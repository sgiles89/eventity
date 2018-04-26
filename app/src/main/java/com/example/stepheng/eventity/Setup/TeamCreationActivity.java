package com.example.stepheng.eventity.Setup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.stepheng.eventity.MainActivity;
import com.example.stepheng.eventity.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class TeamCreationActivity extends AppCompatActivity {

    //declaring UI element variables
    private EditText teamName;
    private Button teamBtn;
    private EditText accessCode;
    private ProgressBar teamCreateProgress;

    //declaring Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore mFStore;

    //adding TAG
    private static final String TAG = "TeamCreationActivity";

    private String user_id;
    private String display_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_creation);

        //set toolbar
        android.support.v7.widget.Toolbar profileSetupToolbar = findViewById(R.id.team_creation_toolbar);
        setSupportActionBar(profileSetupToolbar);
        getSupportActionBar().setTitle("Team Creation");

        //get instances of Firebase auth and firestore
        firebaseAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();

        //assign UI elements
        teamName = findViewById(R.id.team_name);
        teamBtn = findViewById(R.id.team_create_btn);
        accessCode = findViewById(R.id.join_team_code);
        teamCreateProgress = findViewById(R.id.team_create_progress);

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

        //code for button logic
        teamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String team_name = teamName.getText().toString();
                final String access_code = accessCode.getText().toString();
                teamBtn.setEnabled(false);
                teamCreateProgress.setVisibility(View.VISIBLE);
                //if the team name has been entered:
                if (!TextUtils.isEmpty(team_name) && !TextUtils.isEmpty(access_code)){

                    createTeam(team_name, access_code, user_id);

                } else {
                    Toast.makeText(TeamCreationActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void createTeam(final String teamName, String access_code, final String user_id) {
        //create the Team document
        DocumentReference newTeam = mFStore.collection("Teams").document();
        final String teamID = newTeam.getId();
        //create Team data hashmap
        Map<String, Object> teamData = new HashMap<>();
        teamData.put("name", teamName);
        teamData.put("accessCode", access_code);

        //add the team data to the document.
        newTeam.set(teamData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            //continue with creating team
                            WriteBatch batch = mFStore.batch();
                            DocumentReference newMembers = mFStore.collection("Teams/"+teamID+"/Members").document(user_id);
                            //create Member data hashmap
                            Map<String, Object> memberData = new HashMap<>();
                            memberData.put("name", display_name);
                            memberData.put("userID", user_id);
                            memberData.put("role", "owner");
                            //add user as the owner
                            batch.set(newMembers, memberData);
                            DocumentReference userProfile = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("role", "owner");
                            userData.put("userID", user_id);
                            userData.put("teamID", teamID);
                            userData.put("teamName",teamName);
                            batch.set(userProfile,userData);
                            batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //send the user to login
                                    Toast.makeText(TeamCreationActivity.this, "Team Created", Toast.LENGTH_LONG).show();
                                    sendToLogin();
                                }
                            });
                        } else {
                            //display an error to try again later
                            Toast.makeText(TeamCreationActivity.this, "Error creating team. Please try again later", Toast.LENGTH_LONG).show();
                        }
                        //hide the progress bar
                        teamCreateProgress.setVisibility(View.INVISIBLE);
                    }
                });


        }

    private void sendToLogin() {
        Intent mainIntent = new Intent (TeamCreationActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
