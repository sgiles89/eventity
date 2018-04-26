package com.example.stepheng.eventity.AdminPanel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.example.stepheng.eventity.R;
import com.example.stepheng.eventity.Classes.WaitlistMember;
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
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentTeamWait extends Fragment {

    private static final String TAG = "FragmentTeamWait";

    @BindView(R.id.waitlist)
    RecyclerView waitlist;

    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;
    private String user_id;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_waitlist_text) TextView mEmptyListMessage;
    private Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.team_wait_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        DocumentReference teamIDRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
        teamIDRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()){
                        String teamID = document.getString("teamID");
                        getWaitList(teamID);
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
        waitlist.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
    }

    private void getWaitList(final String team_id){
        Query query = mFStore.collection("Teams/"+team_id +"/Waitlist");
        FirestoreRecyclerOptions<WaitlistMember> response = new FirestoreRecyclerOptions.Builder<WaitlistMember>()
                .setQuery(query, WaitlistMember.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<WaitlistMember, WaitlistMemberHolder>(response) {
            @Override
            public void onBindViewHolder(WaitlistMemberHolder holder, int position, final WaitlistMember model) {
                holder.textName.setText(model.getName());

                holder.accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Setup a Write Batch to add the new user to Members, delete them from Waitlist and update their user profile to show membership
                        WriteBatch batch = mFStore.batch();
                        DocumentReference memberList = mFStore.collection("Teams/"+team_id+"/Members").document(model.getUserID());
                        Map<String, Object> newMember = new HashMap<>();
                        newMember.put("name", model.getName());
                        newMember.put("role", "member");
                        newMember.put("userID",model.getUserID());
                        batch.set(memberList, newMember);
                        DocumentReference waitListRef = mFStore.collection("Teams/"+team_id+"/Waitlist").document(model.getUserID());
                        batch.delete(waitListRef);
                        DocumentReference userProfileRef = mFStore.collection("Users/"+model.getUserID()+"/Membership").document("Membership");
                        batch.update(userProfileRef,"role", "member");

                        // Commit the batch
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(getContext(), model.getName()+" was added to the team", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "failure"+task.getException(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });


                    }
                });
                holder.reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        WriteBatch batch = mFStore.batch();
                        DocumentReference waitlistRef = mFStore.collection("Teams/" + team_id + "/Waitlist").document(model.getUserID());
                        DocumentReference userProfileRef = mFStore.collection("Users/" + model.getUserID() + "/Membership").document("Membership");
                        batch.delete(waitlistRef);
                        batch.delete(userProfileRef);
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), model.getName() + " removed from the waitlist", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getContext(), "failure" + task.getException(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            }




            @Override
            public WaitlistMemberHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.waitlist_layout, group, false);

                return new WaitlistMemberHolder(view);
            }

            @Override
            public void onDataChanged() {
                // If there are no chat messages, show a view that invites the user to add a message.
                mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        waitlist.setAdapter(adapter);
        adapter.startListening();
    }

    public class WaitlistMemberHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.waitlist_name)
        TextView textName;
        @BindView(R.id.waitlist_accept)
        Button accept;
        @BindView(R.id.waitlist_reject)
        Button reject;

        public WaitlistMemberHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter !=null){
            adapter.stopListening();
        }
    }
}