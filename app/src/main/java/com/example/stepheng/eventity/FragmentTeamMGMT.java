package com.example.stepheng.eventity;

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

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class FragmentTeamMGMT extends Fragment {

    private static final String TAG = "FragmentTeamMGMT";

    @BindView(R.id.member_list)
    RecyclerView memberlist;

    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;
    private String user_id;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_memberlist_text)
    TextView mEmptyListMessage;
    private Unbinder unbinder;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.team_mgmt_fragment, container, false);
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
                        getMemberlist(teamID);
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
        memberlist.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
    }

    private void getMemberlist(final String team_id){
        Query query = mFStore.collection("Teams/"+team_id +"/Members");
        FirestoreRecyclerOptions<WaitlistMember> response = new FirestoreRecyclerOptions.Builder<WaitlistMember>()
                .setQuery(query, WaitlistMember.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<WaitlistMember, WaitlistMemberHolder>(response) {
            @Override
            public void onBindViewHolder(WaitlistMemberHolder holder, int position, final WaitlistMember model) {
                //users can't remove themselves from a team and the team admin can't be removed
                if (model.getUserID().equals(user_id) || model.getRole().equals("owner")) {
                    holder.adjust.setEnabled(false);
                    holder.remove.setEnabled(false);
                }
                //set the adjust text to "make admin" for users and "demote" for admins
                if (model.getRole().equals("member")){
                    holder.adjust.setText("Make Admin");
                } else {
                    holder.adjust.setText("Demote");
                }
                holder.textName.setText(model.getName());
                holder.adjust.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "adjust was clicked", Toast.LENGTH_LONG).show();
                    }
                });
                holder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(), "remove was clicked", Toast.LENGTH_LONG).show();
                    }
                });
            }



            @Override
            public WaitlistMemberHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.memberlist_layout, group, false);

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
        memberlist.setAdapter(adapter);
        adapter.startListening();
    }

    public class WaitlistMemberHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.member_list_name)
        TextView textName;
        @BindView(R.id.memberlist_adjust)
        Button adjust;
        @BindView(R.id.memberlist_remove)
        Button remove;

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
        adapter.stopListening();
    }
}