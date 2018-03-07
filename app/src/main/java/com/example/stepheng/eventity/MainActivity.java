package com.example.stepheng.eventity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    @BindView(R.id.add_event_btn) private FloatingActionButton nFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Eventity");
        nFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPost = new Intent(MainActivity.this, NewEventActivity.class);
                startActivity(newPost);
                finish();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        if (currentUser == null){
            sendToLogin();
        }
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

            default:
                return false;
        }
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
        Intent profileIntent = new Intent(MainActivity.this, NewEventActivity.class);
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
}
