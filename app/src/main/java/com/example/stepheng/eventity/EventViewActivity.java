package com.example.stepheng.eventity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventViewActivity extends AppCompatActivity {

    //declaring FireBase variables
    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;

    //declaring user,team and event variables
    private String user_id;
    private String TAG = "EventViewActivity";

    //declaring and binding UI elements
    @BindView(R.id.event_title_text) TextView event_title;
    @BindView(R.id.event_date_text)TextView event_date;
    @BindView(R.id.event_time_text)TextView event_time;
    @BindView(R.id.event_description_text)TextView event_description;
    @BindView(R.id.event_location_text)TextView event_location;
    @BindView(R.id.event_going_num)TextView event_going_number;
    @BindView(R.id.event_maybe_num)TextView event_maybe_number;
    @BindView(R.id.event_not_num)TextView event_not_number;
    @BindView(R.id.btn_going) Button going_btn;
    @BindView(R.id.butn_maybe) Button maybe_btn;
    @BindView(R.id.butn_not) Button not_btn;
    @BindView(R.id.ask_q_btn) Button ask_q_btn;


    //recyclerview variables
    @BindView(R.id.questions_view) RecyclerView questionView;
    @BindView(R.id.emptyviewtext) TextView mEmptyListMessage;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        linearLayoutManager = new LinearLayoutManager(EventViewActivity.this, LinearLayoutManager.VERTICAL, false);
        questionView.setLayoutManager(linearLayoutManager);
        Intent intent = getIntent();
        final String event_id = intent.getExtras().getString("event_id");
        //retrieving user's Firebase ID

        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(EventViewActivity.this, LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        //get user's team id and the event information from Firebase
        DocumentReference teamIDRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
        teamIDRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        final String team_id = document.getString("teamID");
                        final String name = document.getString("name");
                        final DocumentReference eventInfoRef = mFStore.collection("Teams/"+team_id+"/Events").document(event_id);
                        eventInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final Event thisEvent = documentSnapshot.toObject(Event.class);
                                //populate fields on the Activity with the event information
                                event_title.setText(thisEvent.getTitle());
                                event_date.setText(thisEvent.getNiceDate());
                                event_time.setText(thisEvent.getTime());
                                event_location.setText(thisEvent.getLocation());
                                event_description.setText((thisEvent.getDescription()));
                                //get going count
                                mFStore.collection("Teams/"+team_id+"/Events/"+event_id+"/Going").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                        if (!documentSnapshots.isEmpty()){
                                            int count = documentSnapshots.size();
                                            updateGoing(count);
                                        } else {
                                            updateGoing(0);
                                        }
                                    }
                                });

                                //get maybe count
                                mFStore.collection("Teams/"+team_id+"/Events/"+event_id+"/Maybe").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                        if (!documentSnapshots.isEmpty()){
                                            int count = documentSnapshots.size();
                                            updateMaybe(count);
                                        } else {
                                            updateMaybe(0);
                                        }
                                    }
                                });

                                //get not count
                                mFStore.collection("Teams/"+team_id+"/Events/"+event_id+"/Not").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                        if (!documentSnapshots.isEmpty()){
                                            int count = documentSnapshots.size();
                                            updateNot(count);
                                        } else {
                                            updateNot(0);
                                        }
                                    }
                                });

                                //button logic
                                going_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final DocumentReference goingRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Going/"+user_id);
                                        final DocumentReference maybeRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Maybe/"+user_id);
                                        final DocumentReference notRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Not/"+user_id);
                                        mFStore.runTransaction(new Transaction.Function<Void>() {
                                            @Override
                                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                                Map<String, Object> goingMap = new HashMap<>();
                                                goingMap.put("name", name);
                                                goingMap.put("userID", user_id);

                                                DocumentSnapshot maybeSnapshot = transaction.get(maybeRef);
                                                DocumentSnapshot notSnapshot = transaction.get(notRef);
                                                if (maybeSnapshot.exists()){
                                                    maybeRef.delete();
                                                    goingRef.set(goingMap);

                                                } else if (notSnapshot.exists()) {
                                                    notRef.delete();
                                                    goingRef.set(goingMap);
                                                } else {
                                                    goingRef.set(goingMap);
                                                }
                                                // Success
                                                return null;
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Transaction success!");
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Transaction failure.", e);
                                                    }
                                                });
                                    }
                                });

                                maybe_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final DocumentReference goingRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Going/"+user_id);
                                        final DocumentReference maybeRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Maybe/"+user_id);
                                        final DocumentReference notRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Not/"+user_id);
                                        mFStore.runTransaction(new Transaction.Function<Void>() {
                                            @Override
                                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                                Map<String, Object> maybeMap = new HashMap<>();
                                                maybeMap.put("name", name);
                                                maybeMap.put("userID", user_id);

                                                DocumentSnapshot goingSnapshot = transaction.get(goingRef);
                                                DocumentSnapshot notSnapshot = transaction.get(notRef);
                                                if (goingSnapshot.exists()){
                                                    goingRef.delete();
                                                    maybeRef.set(maybeMap);

                                                } else if (notSnapshot.exists()) {
                                                    notRef.delete();
                                                    maybeRef.set(maybeMap);
                                                } else {
                                                    maybeRef.set(maybeMap);
                                                }
                                                // Success
                                                return null;
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Transaction success!");
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Transaction failure.", e);
                                                    }
                                                });
                                    }
                                });

                                not_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final DocumentReference goingRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Going/"+user_id);
                                        final DocumentReference maybeRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Maybe/"+user_id);
                                        final DocumentReference notRef = mFStore.document("Teams/"+team_id+"/Events/"+event_id+"/Not/"+user_id);
                                        mFStore.runTransaction(new Transaction.Function<Void>() {
                                            @Override
                                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                                Map<String, Object> notMap = new HashMap<>();
                                                notMap.put("name", name);
                                                notMap.put("userID", user_id);

                                                DocumentSnapshot maybeSnapshot = transaction.get(maybeRef);
                                                DocumentSnapshot goingSnapshot = transaction.get(goingRef);
                                                if (maybeSnapshot.exists()){
                                                    maybeRef.delete();
                                                    notRef.set(notMap);

                                                } else if (goingSnapshot.exists()) {
                                                    goingRef.delete();
                                                    notRef.set(notMap);
                                                } else {
                                                    notRef.set(notMap);
                                                }
                                                // Success
                                                return null;
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Transaction success!");
                                            }
                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Transaction failure.", e);
                                                    }
                                                });
                                    }
                                });

                                Query query = mFStore.collection("Teams/"+team_id+"/Questions/")
                                        .whereEqualTo("eventID", event_id)
                                        .orderBy("questiontime", Query.Direction.ASCENDING);

                                FirestoreRecyclerOptions<Question> response = new FirestoreRecyclerOptions.Builder<Question>()
                                        .setQuery(query, Question.class)
                                        .build();

                                adapter = new FirestoreRecyclerAdapter<Question, EventViewActivity.QuestionHolder>(response) {
                                    @Override
                                    public void onBindViewHolder(EventViewActivity.QuestionHolder holder, int position, final Question model) {
                                        holder.questionText.setText(model.getQuestion());
                                        holder.byText.setText("by "+model.getAsker()+" on "+model.getQuestiontime());
                                        if (!model.isAnswered()){
                                            holder.aText.setVisibility(View.INVISIBLE);
                                            holder.answerText.setVisibility(View.INVISIBLE);
                                        } else {
                                            holder.answerText.setText(model.getAnswer());
                                        }
                                    }



                                    @Override
                                    public QuestionHolder onCreateViewHolder(ViewGroup group, int i) {
                                        View view = LayoutInflater.from(group.getContext())
                                                .inflate(R.layout.qna_layout, group, false);
                                        return new QuestionHolder(view);
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
                                questionView.setAdapter(adapter);
                                adapter.startListening();

                                ask_q_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        askQuestion();
                                    }
                                });

                            }
                        });
                    } else {
                        Log.d(TAG, "profile has no team");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });



    }

    public class QuestionHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.q_text) TextView qText;
        @BindView(R.id.a_text) TextView aText;
        @BindView(R.id.by_text) TextView byText;
        @BindView(R.id.question_text)TextView questionText;
        @BindView(R.id.answer_text) TextView answerText;


        public QuestionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
    public void updateGoing(int count){
        event_going_number.setText(""+count);
    }

    public void updateMaybe(int count){
        event_maybe_number.setText(""+count);
    }

    public void updateNot(int count){
        event_not_number.setText(""+count);
    }

    public static class AskQuestionFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final EditText question = new EditText(getContext());
            // 1. Instantiate an AlertDialog.Builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setTitle(R.string.question_dialog_title)
                    .setView(question);

            builder.setPositiveButton(R.string.question_dialog_positive, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                }
            });
            builder.setNegativeButton(R.string.question_dialog_negative, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            // 3. Get the AlertDialog from create()
            return builder.create();
        }
    }

    public void askQuestion() {
        DialogFragment newFragment = new AskQuestionFragment();
        newFragment.show(getSupportFragmentManager(), "question");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
