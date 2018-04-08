package com.example.stepheng.eventity;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
    private android.support.v7.widget.Toolbar toolbar;

    //declaring and binding UI elements
    @BindView(R.id.event_date_day)TextView event_day;
    @BindView(R.id.event_date_month) TextView event_month;
    @BindView(R.id.event_day_time) TextView event_day_time;
    @BindView(R.id.event_description_text)TextView event_description;
    @BindView(R.id.event_locaton_view)TextView event_location;
    @BindView(R.id.event_going_num)TextView event_going_number;
    @BindView(R.id.event_maybe_num)TextView event_maybe_number;
    @BindView(R.id.event_not_num)TextView event_not_number;
    @BindView(R.id.ask_q_txt) TextView ask_q_txt;
    @BindView(R.id.rsvp_spinner) Spinner rsvp_spinner;

    final FragmentManager fm = getFragmentManager();
    final ListFragment lf = new ListFragment();

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
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.rsvp_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rsvp_spinner.setAdapter(spinnerAdapter);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        toolbar = findViewById(R.id.event_view_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                        findRSVP(team_id, event_id);
                        final DocumentReference eventInfoRef = mFStore.collection("Teams/"+team_id+"/Events").document(event_id);
                        eventInfoRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final Event thisEvent = documentSnapshot.toObject(Event.class);
                                //populate fields on the Activity with the event information
                                getSupportActionBar().setTitle(thisEvent.getTitle());
                                event_day.setText(thisEvent.getDay());
                                event_month.setText(thisEvent.getMonth());
                                event_day_time.setText(thisEvent.getDayofWeek()+" "+thisEvent.getTime());
                                event_location.setText(thisEvent.getLocation());
                                event_description.setText((thisEvent.getDescription()));

                                //spinner logic
                                rsvp_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        if (rsvp_spinner.getSelectedItem().toString().equals("Going")){
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
                                        } else if (rsvp_spinner.getSelectedItem().toString().equals("Maybe")){
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
                                        } else if (rsvp_spinner.getSelectedItem().toString().equals("Not Going")){
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
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
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
                                        holder.byText.setText("by "+model.getAsker()+" on "+model.getNiceQuestiontime());
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

                                event_going_number.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ListFragment df = ListFragment.newInstance(team_id, event_id, "Going");
                                        df.show(getFragmentManager(),"Going");
                                    }
                                });

                                event_not_number.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ListFragment df = ListFragment.newInstance(team_id, event_id, "Not");
                                        df.show(getFragmentManager(),"Not Going");
                                    }
                                });

                                event_maybe_number.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ListFragment df = ListFragment.newInstance(team_id, event_id, "Maybe");
                                        df.show(getFragmentManager(),"Maybe");
                                    }
                                });

                                ask_q_txt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        final EditText question = new EditText(EventViewActivity.this);
                                        new AlertDialog.Builder(EventViewActivity.this)
                                                .setTitle("Ask a question")
                                                .setMessage("Your question:")
                                                .setView(question)
                                                .setPositiveButton("Answer", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        String myQuestion = question.getText().toString();
                                                        Calendar cal = Calendar.getInstance();
                                                        Date questionTime = cal.getTime();
                                                        DocumentReference addNewQuestion = mFStore.collection("Teams/"+team_id+"/Questions").document();
                                                        String questionID = addNewQuestion.getId();
                                                        Question newQuestion = new Question(myQuestion, null, name, user_id, null, null, questionTime, null, false, event_id, questionID);
                                                        addNewQuestion.set(newQuestion).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    Toast.makeText(EventViewActivity.this, "Question Submitted", Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    Toast.makeText(EventViewActivity.this, "Question submission failed. Please try again later", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                })
                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                    }
                                                })
                                                .show();
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


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.admin_event_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.admin_edit_event:
                Intent editIntent = new Intent(EventViewActivity.this, EditEventActivity.class);
                startActivity(editIntent);
                return true;

            case R.id.admin_delete_event:

                return true;

            default:
                return false;
        }
    }

    public void findRSVP (String teamID, String eventID){
        final DocumentReference goingRef = mFStore.document("Teams/"+teamID+"/Events/"+eventID+"/Going/"+user_id);
        final DocumentReference maybeRef = mFStore.document("Teams/"+teamID+"/Events/"+eventID+"/Maybe/"+user_id);
        final DocumentReference notRef = mFStore.document("Teams/"+teamID+"/Events/"+eventID+"/Not/"+user_id);
        mFStore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot maybeSnapshot = transaction.get(maybeRef);
                DocumentSnapshot goingSnapshot = transaction.get(goingRef);
                DocumentSnapshot notSnapshot = transaction.get(notRef);
                if (maybeSnapshot.exists()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rsvp_spinner.setSelection(2);
                        }
                    });
                } else if (goingSnapshot.exists()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rsvp_spinner.setSelection(1);
                        }
                    });
                    Log.d(TAG, "set to Going");
                } else if (notSnapshot.exists()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rsvp_spinner.setSelection(3);

                        }
                    });
                } else {
                    rsvp_spinner.setSelection(0);
                    Log.d(TAG, "set to undecided");
                }
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
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
