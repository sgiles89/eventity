package com.example.stepheng.eventity;


import android.content.Intent;
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

public class FragmentHome extends Fragment {

    private String TAG = "FragmentHome";
    private String user_id;
    private Date currentDate = Calendar.getInstance().getTime();

    @BindView(R.id.upcoming_view)
    RecyclerView upcomingView;
    @BindView(R.id.past_view)
    RecyclerView pastView;

    //declare Firebase variables
    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;

    //declare RecyclerView and Butterknife variables
    private FirestoreRecyclerAdapter upAdapter;
    private FirestoreRecyclerAdapter pastAdapter;
    LinearLayoutManager upcomingLLM;
    LinearLayoutManager pastLLM;
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

        //retrieving user's Firebase ID

        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(getContext(), LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        init();
        DocumentReference teamIDRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
        teamIDRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()){
                        String teamID = document.getString("teamID");
                        getUpcoming(teamID);
                        getPast(teamID);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        return view;
    }

    private void init(){
        upcomingLLM = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        pastLLM = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        upcomingView.setLayoutManager(upcomingLLM);
        pastView.setLayoutManager(pastLLM);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
    }

    private void getUpcoming(final String team_id){
        Query upQuery = mFStore.collection("Teams/"+team_id+"/Events")
                .whereGreaterThan("date", currentDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(3);

        FirestoreRecyclerOptions<Event> response = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(upQuery, Event.class)
                .build();

        upAdapter = new FirestoreRecyclerAdapter<Event, EventHolder>(response) {
            @Override
            public void onBindViewHolder(EventHolder holder, int position, final Event model) {
                holder.eventTitle.setText(model.getTitle());
                holder.dayText.setText(model.getDay());
                holder.monthText.setText(model.getMonth());
                holder.timeText.setText(model.getTime());
                holder.locationText.setText(model.getLocation());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), EventViewActivity.class);
                        String eventID = model.getEventID();
                        i.putExtra("event_id", eventID);
                        i.putExtra("team_id", team_id);
                        startActivity(i);
                    }
                });
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

        upAdapter.notifyDataSetChanged();
        upcomingView.setAdapter(upAdapter);
        upAdapter.startListening();
    }

    private void getPast(final String team_id){
        Query pastQuery = mFStore.collection("Teams/"+team_id+"/Events")
                .whereLessThan("date", currentDate)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(3);

        FirestoreRecyclerOptions<Event> response = new FirestoreRecyclerOptions.Builder<Event>()
                .setQuery(pastQuery, Event.class)
                .build();

        pastAdapter = new FirestoreRecyclerAdapter<Event, EventHolder>(response) {
            @Override
            public void onBindViewHolder(EventHolder holder, int position, final Event model) {
                holder.eventTitle.setText(model.getTitle());
                holder.dayText.setText(model.getDay());
                holder.monthText.setText(model.getMonth());
                holder.timeText.setText(model.getTime());
                holder.locationText.setText(model.getLocation());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), EventViewActivity.class);
                        String eventID = model.getEventID();
                        i.putExtra("event_id", eventID);
                        i.putExtra("team_id", team_id);
                        startActivity(i);
                    }
                });
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

        pastAdapter.notifyDataSetChanged();
        pastView.setAdapter(pastAdapter);
        pastAdapter.startListening();
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
        if (upAdapter != null && pastAdapter != null){
            pastAdapter.startListening();
            upAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (upAdapter != null && pastAdapter != null){
            pastAdapter.stopListening();
            upAdapter.stopListening();
        }
    }

    /*
    @Override
    public void onResume(){
        super.onResume();
        pastAdapter.startListening();
        upAdapter.startListening();
    }
    */

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
