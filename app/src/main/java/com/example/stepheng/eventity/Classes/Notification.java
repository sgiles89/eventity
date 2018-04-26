package com.example.stepheng.eventity.Classes;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Notification {
    @ServerTimestamp private Date createdAt;
    private String title;
    private String message;
    private String notify_type;

    public Notification(){}

    public Notification(Date createdAt, String title, String message, String notify_type) {
        this.createdAt = createdAt;
        this.title = title;
        this.message = message;
        this.notify_type = notify_type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotify_type() {
        return notify_type;
    }

    public void setNotify_type(String notify_type) {
        this.notify_type = notify_type;
    }
}
