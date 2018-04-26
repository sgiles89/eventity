package com.example.stepheng.eventity.AdminPanel;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.stepheng.eventity.Classes.Question;
import com.example.stepheng.eventity.R;
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

public class FragmentQuestionMGMT extends Fragment {

    private static final String TAG = "FragmentQuestionMGMT";

    @BindView(R.id.admin_questions_view)
    RecyclerView questionsView;

    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;
    private String user_id;
    private FirestoreRecyclerAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    @BindView(R.id.empty_admin_questions)
    TextView mEmptyListMessage;
    private Unbinder unbinder;
    Calendar cal = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.question_mgmt_fragment, container, false);
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
                        String name = document.getString("name");
                        getQuestions(teamID, name);
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
        questionsView.setLayoutManager(linearLayoutManager);
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
    }

    private void getQuestions(final String team_id, final String name){
        Query query = mFStore.collection("Teams/"+team_id +"/Questions")
                .whereEqualTo("answered", false);
        FirestoreRecyclerOptions<Question> response = new FirestoreRecyclerOptions.Builder<Question>()
                .setQuery(query, Question.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Question, QuestionHolder>(response) {
            @Override
            public void onBindViewHolder(QuestionHolder holder, int position, final Question model) {
                holder.questionText.setText(model.getQuestion());
                holder.byText.setText(model.getAsker());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText answer = new EditText(getContext());
                        new AlertDialog.Builder(getContext())
                                .setTitle("Question")
                                .setMessage(model.getQuestion())
                                .setView(answer)
                                .setPositiveButton("Answer", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        String answerText = answer.getText().toString();
                                        Date answerTime = cal.getTime();
                                        Question askedQuestion = model;
                                        askedQuestion.setAnswer(answerText);
                                        askedQuestion.setAnswered(true);
                                        askedQuestion.setAnswerer(name);
                                        askedQuestion.setAnswererID(user_id);
                                        askedQuestion.setAnswertime(answerTime);
                                        DocumentReference questionRef = mFStore.collection("Teams/"+team_id+"/Questions").document(model.getQuestionID());
                                        questionRef.set(askedQuestion);
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

            @Override
            public QuestionHolder onCreateViewHolder(ViewGroup group, int i) {
                View view = LayoutInflater.from(group.getContext())
                        .inflate(R.layout.admin_questions_layout, group, false);

                return new QuestionHolder(view);
            }

            @Override
            public void onDataChanged() {
                mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                Log.e("error", e.getMessage());
            }
        };

        adapter.notifyDataSetChanged();
        questionsView.setAdapter(adapter);
        adapter.startListening();
    }

    public class QuestionHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.admin_question_text)
        TextView questionText;
        @BindView(R.id.admin_question_by)
        TextView byText;

        public QuestionHolder(View itemView) {
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