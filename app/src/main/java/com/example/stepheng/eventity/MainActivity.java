package com.example.stepheng.eventity;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFStore;
    @BindView(R.id.add_event_btn) FloatingActionButton nFAB;
    @BindView(R.id.mainBottomNav) BottomNavigationView mainBottomNav;
    private FragmentHome homeFragment;
    private FragmentEvents eventsFragment;
    private FragmentNotifications notificationsFragment;
    private FragmentNoTeam noTeamFragment;
    private FragmentPendingTeam pendingTeamFragment;
    private String user_id;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();

        //check for logged in user
        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        //Setting up fragments
        homeFragment = new FragmentHome();
        notificationsFragment = new FragmentNotifications();
        eventsFragment = new FragmentEvents();
        noTeamFragment = new FragmentNoTeam();
        pendingTeamFragment = new FragmentPendingTeam();

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Eventity");
        nFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPost = new Intent(MainActivity.this, NewEventActivity.class);
                startActivity(newPost);
            }
        });

        //check for pending/no team
        checkTeamStatus();


        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.bottom_action_home :
                        switchFragment(homeFragment);
                        return true;

                    case R.id.bottom_action_events :
                        switchFragment(eventsFragment);
                        return true;

                    case R.id.bottom_action_notifications :
                        switchFragment(notificationsFragment);
                        return true;

                    default:
                        return false;
                }


            }
        });
    }

    private void checkTeamStatus() {
        DocumentReference teamRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
        teamRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d(TAG, "document exists");
                        if (document.getString("role").equals("pending")){
                            nFAB.setVisibility(View.INVISIBLE);
                            switchFragment(pendingTeamFragment);
                        } else if(document.getString("role").equals("owner") || document.getString("role").equals("admin")) {
                            switchFragment(homeFragment);
                        } else if(document.getString("role").equals("user")){
                            nFAB.setVisibility(View.INVISIBLE);
                            switchFragment(homeFragment);
                        }
                    } else {
                        switchFragment(noTeamFragment);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_logout_btn:
                logOut();
                return true;

            case R.id.action_profile_button:
                sendToProfile();
                return true;

            case R.id.action_admin_panel:
                sendtoAdminMain();
                return true;

            case R.id.action_join_team:
                sentToJoinTeam();
                return true;

            case R.id.action_create_team:
                sendtoCreateTeam();
                return true;

            default:
                return false;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        checkTeamStatus();
    }

    private void sendtoCreateTeam() {
        Intent createTeamIntent = new Intent(MainActivity.this, TeamCreationActivity.class);
        startActivity(createTeamIntent);
        finish();
    }

    private void sentToJoinTeam() {
        Intent JoinTeamItent = new Intent(MainActivity.this, JoinTeamActivity.class);
        startActivity(JoinTeamItent);
        finish();
    }

    private void sendtoAdminMain() {
        Intent mainAdminItent = new Intent(MainActivity.this, AdminMainActivity.class);
        startActivity(mainAdminItent);
        finish();
    }

    private void sendToProfile() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileSetupActivity.class);
        startActivity(profileIntent);
        finish();
    }

    private void logOut() {

        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void switchFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }
}
