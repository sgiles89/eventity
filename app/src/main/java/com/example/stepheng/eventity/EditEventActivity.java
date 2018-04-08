package com.example.stepheng.eventity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditEventActivity extends AppCompatActivity {
    //declaring and assigning UI elements
    @BindView(R.id.new_event_toolbar) android.support.v7.widget.Toolbar newEventToolbar;
    @BindView(R.id.event_title) EditText eventName;
    @BindView(R.id.event_desc) EditText eventDesc;
    @BindView(R.id.event_date) EditText eventDate;
    @BindView(R.id.event_location) EditText eventLocation;
    @BindView(R.id.event_time) EditText eventTime;

    //date and time picker variables
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
    private String time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        //set Toolbar
        ButterKnife.bind(this);
        setSupportActionBar(newEventToolbar);
        getSupportActionBar().setTitle("New Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        //get instances of Firebase
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };


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
                // TODO Auto-generated method stub
                new DatePickerDialog(EditEventActivity.this, date, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(EditEventActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                eventTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

    }

    private void updateLabel() {
        String myFormat = "EEEE, MMMM dd YYYY"; //
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        String saveFormat = "yyyy-M-d";
        SimpleDateFormat nsdf = new SimpleDateFormat(saveFormat);
        setTime(nsdf.format((calendar.getTime())));
        eventDate.setText(sdf.format(calendar.getTime()));
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(EditEventActivity.this, MainActivity.class);
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

    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.new_event_save, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.save_btn:
                item.setEnabled(false);
                //get data from all the fields
                final String event_title = eventName.getText().toString();
                final String event_description = eventDesc.getText().toString();
                final String event_location = eventLocation.getText().toString();
                final String event_date = eventDate.getText().toString();
                final String event_time = eventTime.getText().toString();
                final Date event_time_and_date = getDateFromString(time+"T"+event_time+"Z");
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
                                Toast.makeText(EditEventActivity.this, "Event created", Toast.LENGTH_LONG).show();
                                sendToMain();
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
                return true;


            default:
                return false;
        }
    }

    public void setTime(String time){
        this.time = time;
    }
    public void setTeamID(String teamid){
        this.team_id = teamid;
    }
}
