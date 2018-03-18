package com.example.stepheng.eventity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ParseException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewEventActivity extends AppCompatActivity {
    //declaring and assigning UI elements
    @BindView(R.id.new_event_toolbar) android.support.v7.widget.Toolbar newEventToolbar;
    @BindView(R.id.event_title) EditText eventName;
    @BindView(R.id.event_desc) EditText eventDesc;
    @BindView(R.id.event_date) TextView eventDate;
    @BindView(R.id.event_location) EditText eventLocation;
    @BindView(R.id.event_time) EditText eventTime;
    @BindView(R.id.create_event_btn) Button eventCreate;

    //date and time picker variables
    private DatePickerDialog.OnDateSetListener eventdateSetListener;
    private int year;
    private int month;
    private int dayOfMonth;
    private int hour;
    private int minute;
    private Calendar calendar;

    private String user_id;
    private String team_id;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFStore;

    private final String TAG = "NewEventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        //set Toolbar
        ButterKnife.bind(this);
        setSupportActionBar(newEventToolbar);
        getSupportActionBar().setTitle("New Event");

        //get instances of Firebase
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

        //adding a DatePicker for date field
        eventDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        calendar = Calendar.getInstance();
                        year = calendar.get(Calendar.YEAR);
                        month = calendar.get(Calendar.MONTH);
                        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog datePickerDialog = new DatePickerDialog(NewEventActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        eventDate.setText(year + "-" + (month+1) + "-" +dayOfMonth );
                                    }
                                }, year, month, dayOfMonth);
                        datePickerDialog.show();
            }
        });

        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(NewEventActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                eventTime.setText(hourOfDay+":"+minute);
                            }
                        }, hour, minute, DateFormat.is24HourFormat(NewEventActivity.this));
                timePickerDialog.show();
            }
        });


        eventCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get data from all the fields
                final String event_title = eventName.getText().toString();
                final String event_description = eventDesc.getText().toString();
                final String event_location = eventLocation.getText().toString();
                final String event_date = eventDate.getText().toString();
                final String event_time = eventTime.getText().toString();
                final Date event_time_and_date = getDateFromString(event_date+"T"+event_time+"Z");
                Log.d(TAG, "The event time was: "+event_time);
                //store event in Firestore
                DocumentReference teamIDRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
                teamIDRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()){
                                String teamID = document.getString("teamID");
                                DocumentReference newEvent = mFStore.collection("Teams/"+team_id+"/Events").document();
                                final String event_id = newEvent.getId();
                                newEvent.set(new Event(event_title, event_time_and_date,user_id, event_location,event_description, event_time, event_id));

                                //add a success toast and send to Main Activity
                                Toast.makeText(NewEventActivity.this, "Event created", Toast.LENGTH_LONG);
                                sendToMain();
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });


            }
        });


    }

    private void sendToMain() {
        Intent mainIntent = new Intent(NewEventActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d'T'HH:mm'Z'");
    public Date getDateFromString(String datetoSaved){

        try {
            Date date = format.parse(datetoSaved);
            return date ;
        } catch (ParseException e){
            return null ;
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return null;
        }

    }
    public void setTeamID(String teamid){
        this.team_id = teamid;
    }
}
