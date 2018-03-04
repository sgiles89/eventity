package com.example.stepheng.eventity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdminMainActivity extends AppCompatActivity {
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.waitlist)
    RecyclerView friendList;

    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        ButterKnife.bind(this);
        init();
        getFriendList();
    }

    private void init(){
        linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        friendList.setLayoutManager(linearLayoutManager);
        db = FirebaseFirestore.getInstance();
    }

    private void getFriendList(){
        Query query = db.collection("Teams/YfLa27NWaaQSfNwhZPgX/Waitlist");

        FirestoreRecyclerOptions<WaitlistMember> response = new FirestoreRecyclerOptions.Builder<WaitlistMember>()
                .setQuery(query, WaitlistMember.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<WaitlistMember, WaitlistMemberHolder>(response) {
            @Override
            public void onBindViewHolder(WaitlistMemberHolder holder, int position, final WaitlistMember model) {
                progressBar.setVisibility(View.GONE);
                holder.textName.setText(model.getName());

                holder.accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Setup a Write Batch to add the new user to Members, delete them from Waitlist and update their user profile to show membership
                        WriteBatch batch = db.batch();
                        DocumentReference memberList = db.collection("Teams/YfLa27NWaaQSfNwhZPgX/Members").document(model.getUserID());
                        Map<String, Object> newMember = new HashMap<>();
                        newMember.put("name", model.getName());
                        newMember.put("role", "user");
                        newMember.put("userID",model.getUserID());
                        batch.set(memberList, newMember);
                        DocumentReference waitListRef = db.collection("Teams/YfLa27NWaaQSfNwhZPgX/Waitlist").document(model.getUserID());
                        batch.delete(waitListRef);
                        DocumentReference userProfileRef = db.collection("Users/"+model.getUserID()+"/Membership").document("Membership");
                        batch.update(userProfileRef,"role", "user");

                        // Commit the batch
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(AdminMainActivity.this, model.getName()+" was added to the team", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(AdminMainActivity.this, "failure"+task.getException(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });


                    }
                });
                holder.reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(AdminMainActivity.this, model.getName()+" was rejected", Toast.LENGTH_LONG).show();
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
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        friendList.setAdapter(adapter);
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
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}