package com.example.stepheng.eventity;


import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.stepheng.eventity.AdminPanel.AdminFragment;
import com.example.stepheng.eventity.Home.MainFragment;
import com.example.stepheng.eventity.Setup.JoinTeamActivity;
import com.example.stepheng.eventity.Setup.LoginActivity;
import com.example.stepheng.eventity.Setup.ProfileSetupActivity;
import com.example.stepheng.eventity.Setup.TeamCreationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mainToolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFStore;
    private MainFragment mainFragment;
    private AdminFragment adminFragment;
    private CircleImageView navProfileImage;
    private Uri profileImageURI = null;
    private String user_id;
    private View navHeader;
    private TextView navName;
    private TextView navRole;

    private String TAG = "MainActivity";
    @BindView(R.id.drawer_layout) DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //check for logged in user
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = mAuth.getInstance().getCurrentUser().getUid();

        }
        Log.d(TAG, "the user is = "+user_id);
        initFCM();

        mainFragment = new MainFragment();
        adminFragment = new AdminFragment();

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Eventity");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);


        NavigationView navigationView = findViewById(R.id.nav_view);
        checkTeamStatus();
        navHeader = navigationView.getHeaderView(0);
        navProfileImage = navHeader.findViewById(R.id.user_profile_img);
        navName = navHeader.findViewById(R.id.navbar_name);
        navRole = navHeader.findViewById(R.id.navbar_role);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        switch (menuItem.getItemId()){
                            case R.id.nav_home:
                                switchFragment(mainFragment);
                                break;

                            case R.id.nav_admin:
                                switchFragment(adminFragment);
                                break;
                        }


                        return true;
                    }
                });







        final DocumentReference profileRef = mFStore.document("Users/"+user_id);
        final DocumentReference membershipRef = mFStore.document("Users/"+user_id+"/Membership/Membership");

        profileRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            String name = task.getResult().getString("name");
                            String image = task.getResult().getString("image");
                            Glide.with(MainActivity.this).load(image).into(navProfileImage);
                            navName.setText(name);
                        }
                    }
                });
        membershipRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String team = document.getString("teamName");
                                String role = document.getString("role");
                                if (role.equals("pending")){
                                    navRole.setVisibility(View.GONE);
                                }else {
                                    String capitalisedrole = role.substring(0, 1).toUpperCase() + role.substring(1);
                                    navRole.setText(capitalisedrole + " of " + team);
                                }

                            } else {
                                navRole.setVisibility(View.GONE);
                                Log.d(TAG, "No such document");
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

                case android.R.id.home:
                    mDrawerLayout.openDrawer(GravityCompat.START);
                    return true;

                default:
                    return false;
            }
        }
    @Override
    protected void onStart(){
        super.onStart();
        switchFragment(mainFragment);
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
        switchFragment(adminFragment);
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
        fragmentTransaction.replace(R.id.fragment_holder, fragment);
        fragmentTransaction.commit();
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
                        if(document.getString("role").equals("owner") || document.getString("role").equals("admin")) {
                        } else {
                            hideItem();
                        }
                    } else {
                        hideItem();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        if (user_id == null){
            sendToLogin();
        } else {
            DocumentReference regToken = mFStore.collection("Users").document(user_id);
            regToken.update("messaging_token", token);
        }
    }


    private void initFCM(){
        String token = FirebaseInstanceId.getInstance().getToken();
        if(token!=null){ Log.d(TAG, "initFCM: token: " + token);
        sendRegistrationToServer(token);
        }


    }

    private void hideItem()
    {
        NavigationView navView = findViewById(R.id.nav_view);
        Menu nav_Menu = navView.getMenu();
        nav_Menu.findItem(R.id.nav_admin).setVisible(false);
    }
}
