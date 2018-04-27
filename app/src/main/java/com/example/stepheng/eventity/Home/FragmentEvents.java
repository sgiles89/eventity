package com.example.stepheng.eventity.Home;


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
import android.widget.TextView;

import com.example.stepheng.eventity.Classes.Event;
import com.example.stepheng.eventity.Events.EventViewActivity;
import com.example.stepheng.eventity.R;
import com.example.stepheng.eventity.Setup.LoginActivity;
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
public class FragmentEvents extends Fragment {

    private String TAG = "FragmentEvents";
    private String user_id;
    private Date currentDate = Calendar.getInstance().getTime();



    //declare Firebase variables
    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;

    //declare RecyclerView and Butterknife variables
    @BindView(R.id.event_view)
    RecyclerView eventView;
    @BindView(R.id.empty_events_message) TextView mEmptyListMessage;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    private Unbinder unbinder;

    public FragmentEvents() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fragment_events, container, false);

        unbinder = ButterKnife.bind(this, view);

        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();

        //retrieving user's Firebase ID and their Team ID

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
                        getEvents(teamID);
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
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        eventView.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
    }

    private void getEvents(String team_id){
        Query  query = mFStore.collection("Teams/"+team_id+"/Events")
                .whereGreaterThan("date", currentDate)
                .orderBy("date", Query.Direction.ASCENDING);

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
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getContext(), EventViewActivity.class);
                        String eventID = model.getEventID();
                        i.putExtra("event_id", eventID);
                        Log.d(TAG, "I put "+eventID+" into the intent");
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

        adapter.notifyDataSetChanged();
        eventView.setAdapter(adapter);
        adapter.startListening();
    }

    public class EventHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.event_title_text)
        TextView eventTitle;
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
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null){
            adapter.stopListening();
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}