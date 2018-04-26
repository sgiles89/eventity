package com.example.stepheng.eventity.Events;

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

import com.example.stepheng.eventity.Classes.Event;
import com.example.stepheng.eventity.MainActivity;
import com.example.stepheng.eventity.R;
import com.example.stepheng.eventity.Setup.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditEventActivity extends AppCompatActivity {
    //declaring and assigning UI elements
    @BindView(R.id.edit_event_toolbar) android.support.v7.widget.Toolbar newEventToolbar;
    @BindView(R.id.eddittext_event_title) EditText eventName;
    @BindView(R.id.eddittext_event_desc) EditText eventDesc;
    @BindView(R.id.eddittext_event_date) EditText eventDate;
    @BindView(R.id.eddittext_event_location) EditText eventLocation;
    @BindView(R.id.eddittext_event_time) EditText eventTime;

    //date and time picker variables
    private int year;
    private int month;
    private int dayOfMonth;
    private int hour;
    private int minute;
    private Calendar calendar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFStore;

    private final String TAG = "EditEventActivity";
    private String date;
    private String event_id, team_id, user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        Intent intent = getIntent();
        team_id = intent.getExtras().getString("team_id");
        event_id = intent.getExtras().getString("event_id");

        //set Toolbar
        ButterKnife.bind(this);
        setSupportActionBar(newEventToolbar);
        getSupportActionBar().setTitle("Edit Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        //get instances of Firebase
        mAuth = FirebaseAuth.getInstance();
        mFStore = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        //retrieve userID
        if (mAuth.getCurrentUser() == null) {
            Intent newIntent = new Intent(EditEventActivity.this, LoginActivity.class);
            startActivity(newIntent);
        }
        else{
            user_id = mAuth.getInstance().getCurrentUser().getUid();
        }
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


         DocumentReference eventRef = mFStore.collection("Teams/"+team_id+"/Events").document(event_id);
         eventRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
             @Override
             public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                 if (task.isSuccessful()) {
                     Event event = task.getResult().toObject(Event.class);
                     eventName.setText(event.getTitle());
                     eventDesc.setText(event.getDescription());
                     eventLocation.setText(event.getLocation());
                     eventTime.setText(event.getTime());
                     Date eventDAndTime = event.getDate();
                     SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd YYYY");
                     String eventNiceDate = sdf.format(eventDAndTime);
                     eventDate.setText(eventNiceDate);
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
        setDate(nsdf.format((calendar.getTime())));
        eventDate.setText(sdf.format(calendar.getTime()));
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(EditEventActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


    public Date getDateFromString(String datetoSaved){
        SimpleDateFormat format = new SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm", Locale.ENGLISH);
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

        getMenuInflater().inflate(R.menu.edit_event_update, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.update:
                item.setEnabled(false);
                //get data from all the fields
                final String event_title = eventName.getText().toString();
                final String event_description = eventDesc.getText().toString();
                final String event_location = eventLocation.getText().toString();
                final String event_date = eventDate.getText().toString();
                final String event_time = eventTime.getText().toString();
                String combined_datentime = event_date+" "+event_time;
                Log.d(TAG, "the combined time = "+ combined_datentime);
                final Date event_time_and_date = getDateFromString(combined_datentime);
                Log.d(TAG, event_time_and_date.toString());
                //store event in Firestore
                DocumentReference teamIDRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");
                teamIDRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()){
                                String teamID = document.getString("teamID");
                                DocumentReference newEvent = mFStore.collection("Teams/"+team_id+"/Events").document(event_id);
                                Log.d(TAG, "The event path given was "+"Teams/"+team_id+"/Events/"+event_id);
                                final String event_id = newEvent.getId();
                                Event updatedEvent = new Event(event_title, event_time_and_date,user_id, event_location,event_description, event_time, event_id);
                                newEvent.set(updatedEvent);

                                //add a success toast and send to Main Activity
                                Toast.makeText(EditEventActivity.this, "Event updated", Toast.LENGTH_LONG).show();
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

    public void setDate(String time){
        this.date = time;
    }
    public void setTeamID(String teamid){
        this.team_id = teamid;
    }
}
