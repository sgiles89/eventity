package com.example.stepheng.eventity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by stepheng on 05/03/2018.
 */

public class Event {
    private String title;
    private Date date;
    private String creatorID;
    private String location;
    private String description;
    private String time;
    private String eventID;


    public Event(){}

    public Event(String title, Date date, String creatorID, String location, String description, String time, String eventID) {
        this.title = title;
        this.date = date;
        this.creatorID = creatorID;
        this.location = location;
        this.description = description;
        this.time = time;
        this.eventID = eventID;
    }

    public String getNiceDate(){
        SimpleDateFormat niceDate = new SimpleDateFormat("EEE MMM dd yyyy");
        String newNiceDate = niceDate.format(this.date);
        return newNiceDate;
    }

    public String getMonth(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.date);
        String eventMonth;
        int month = cal.get(Calendar.MONTH);
        switch (month) {
            case 0:  eventMonth = "JAN";
                break;
            case 1:  eventMonth = "FEB";
                break;
            case 2:  eventMonth = "MAR";
                break;
            case 3:  eventMonth = "APR";
                break;
            case 4:  eventMonth = "MAY";
                break;
            case 5:  eventMonth = "JUN";
                break;
            case 6:  eventMonth = "JUL";
                break;
            case 7:  eventMonth = "AUG";
                break;
            case 8:  eventMonth = "SEP";
                break;
            case 9: eventMonth = "OCT";
                break;
            case 10: eventMonth = "NOV";
                break;
            case 11: eventMonth = "DEC";
                break;
            default: eventMonth = "Invalid month";
                break;
        }
        return eventMonth;
    }

    public String getDayofWeek(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.date);
        int dayofWeek =  cal.get(Calendar.DAY_OF_WEEK);
        String eventDay;
        switch (dayofWeek){
            case 1: eventDay = "Sunday";
                break;
            case 2: eventDay = "Monday";
                break;
            case 3: eventDay = "Tuesday";
                break;
            case 4: eventDay = "Wednesday";
                break;
            case 5: eventDay = "Thursday";
                break;
            case 6: eventDay = "Friday";
                break;
            case 7: eventDay = "Saturday";
                break;
            default: eventDay = "Invalid Day";
                break;
        }
        return eventDay;
    }

    public String getDay(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.date);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            String dayString = "0" + String.valueOf(day);
            return dayString;
        } else {
            String dayString = String.valueOf(day);
            return dayString;
        }

    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;}

    public void setTime(String time) {
        this.time = time;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}

