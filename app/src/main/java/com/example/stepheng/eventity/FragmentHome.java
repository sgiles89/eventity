package com.example.stepheng.eventity;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHome extends Fragment {

    private String team_id;
    private String user_id;
    private String TAG = "FragmentHome";
    private Date currentDate = Calendar.getInstance().getTime();

    @BindView(R.id.upcoming_view)
    RecyclerView upcomingView;

    //declare Firebase variables
    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    private Unbinder unbinder;

    public FragmentHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();

        //retrieving user's Firebase ID and their Team ID
        user_id = mAuth.getCurrentUser().getUid();
        DocumentReference teamIDRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
        teamIDRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        setTeamID(document.get("teamID").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        init();
        getUpcoming();
        return view;
    }

    private void init(){
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        upcomingView.setLayoutManager(linearLayoutManager);
        mFStore = FirebaseFirestore.getInstance();
    }

    private void getUpcoming(){
        Query query = mFStore.collection("Teams/"+team_id+"/Events")
                .whereGreaterThan("date", currentDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(3);

        FirestoreRecyclerOptions<Event> response = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(query, Event.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Event, EventHolder>(response) {
            @Override
            public void onBindViewHolder(EventHolder holder, int position, final Event model) {
                holder.eventTitle.setText(model.getTitle());
                holder.dayText.setText(model.getDay());
                holder.monthText.setText(model.getMonth());
                holder.timeText.setText(model.getTime());
                holder.locationText.setText(model.getLocation());
            }



            @Override
            public EventHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.eventlist_layout, group, false);

                return new EventHolder(view);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        upcomingView.setAdapter(adapter);
    }

    public class EventHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.event_title_text) TextView eventTitle;
        @BindView(R.id.text_day) TextView dayText;
        @BindView(R.id.text_month) TextView monthText;
        @BindView(R.id.text_time) TextView timeText;
        @BindView(R.id.text_location) TextView locationText;
        @BindView(R.id.static_hyphen) TextView hyphen;


        public EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void setTeamID(String teamid){
        this.team_id = teamid;
    }

}