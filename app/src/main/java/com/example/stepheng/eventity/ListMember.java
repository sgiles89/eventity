package com.example.stepheng.eventity;

public class ListMember {

    String name, user_id;

    ListMember(){

    }

    public ListMember(String name, String user_id) {
        this.name = name;
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}

