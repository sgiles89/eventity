package com.example.stepheng.eventity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    @BindView(R.id.add_event_btn)
    FloatingActionButton nFAB;
    @BindView(R.id.mainBottomNav)
    BottomNavigationView mainBottomNav;
    private FragmentHome homeFragment;
    private FragmentEvents eventsFragment;
    private FragmentNotifications notificationsFragment;
    private FragmentNoTeam noTeamFragment;
    private FragmentPendingTeam pendingTeamFragment;
    private String TAG = "MainFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFStore;
    private String user_id;
    private Unbinder unbinder;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();

        //Setting up fragments
        homeFragment = new FragmentHome();
        notificationsFragment = new FragmentNotifications();
        eventsFragment = new FragmentEvents();
        noTeamFragment = new FragmentNoTeam();
        pendingTeamFragment = new FragmentPendingTeam();

        initFragment();

        nFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPost = new Intent(getContext(), NewEventActivity.class);
                startActivity(newPost);
            }
        });

        //if not logged in, send to login
        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = mAuth.getInstance().getCurrentUser().getUid();
        }
        checkTeamStatus();

        mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.bottom_action_home :
                        replaceFragment(homeFragment);
                        return true;

                    case R.id.bottom_action_events :
                        replaceFragment(eventsFragment);
                        return true;

                    case R.id.bottom_action_notifications :
                        replaceFragment(notificationsFragment);
                        return true;

                    default:
                        return false;
                }


            }
        });

        return view;
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
                            replaceFragment(pendingTeamFragment);
                        } else if(document.getString("role").equals("owner") || document.getString("role").equals("admin")) {
                            replaceFragment(homeFragment);
                        } else if(document.getString("role").equals("member")){
                            nFAB.setVisibility(View.INVISIBLE);
                            replaceFragment(homeFragment);
                        }
                    } else {
                        Log.d(TAG, "document don't exist");
                        nFAB.setVisibility(View.INVISIBLE);
                        replaceFragment(noTeamFragment);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void initFragment(){

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, notificationsFragment);
        fragmentTransaction.add(R.id.main_container, eventsFragment);

        fragmentTransaction.hide(notificationsFragment);
        fragmentTransaction.hide(eventsFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        if(fragment == homeFragment){

            fragmentTransaction.hide(eventsFragment);
            fragmentTransaction.hide(notificationsFragment);

        }

        if(fragment == eventsFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationsFragment);

        }

        if(fragment == notificationsFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(eventsFragment);

        }
        fragmentTransaction.show(fragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }

}
