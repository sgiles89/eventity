package com.example.stepheng.eventity;

/**
 * Created by Stephen on 03/03/2018.
 */

public class WaitlistMember {

    private String name, userID, role;

    public WaitlistMember () {}

    public WaitlistMember(String name, String userID, String role){
        this.name = name;
        this.userID = userID;
        this.role = role;
}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}