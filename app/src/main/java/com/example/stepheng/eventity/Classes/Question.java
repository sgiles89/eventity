package com.example.stepheng.eventity.Classes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Question {
    private String question;
    private String answer;
    private String asker;
    private String askerID;
    private String answerer;
    private String answererID;
    private Date questiontime;
    private Date answertime;
    private boolean isAnswered;
    private String eventID;
    private String questionID;

    public Question(){

    }

    public Question(String question, String answer, String asker, String askerID, String answerer, String answererID, Date questiontime, Date answertime, boolean isAnswered, String eventID, String questionID) {
        this.question = question;
        this.answer = answer;
        this.asker = asker;
        this.askerID = askerID;
        this.answerer = answerer;
        this.answererID = answererID;
        this.questiontime = questiontime;
        this.answertime = answertime;
        this.isAnswered = isAnswered;
        this.eventID = eventID;
        this.questionID = questionID;
    }


    public String getNiceQuestiontime() {

        SimpleDateFormat niceDate = new SimpleDateFormat("MMMM dd yyyy");
        String niceQuestiontime = niceDate.format(this.questiontime);
        return niceQuestiontime;
    }


    public String getNiceAnswertime() {
        SimpleDateFormat niceDate = new SimpleDateFormat("MMMM dd yyyy");
        String niceAnswertime = niceDate.format(this.questiontime);
        return niceAnswertime;
    }





    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAsker() {
        return asker;
    }

    public void setAsker(String asker) {
        this.asker = asker;
    }

    public String getAnswerer() {
        return answerer;
    }

    public void setAnswerer(String answerer) {
        this.answerer = answerer;
    }

    public String getAskerID() {
        return askerID;
    }

    public void setAskerID(String askerID) {
        this.askerID = askerID;
    }

    public String getAnswererID() {
        return answererID;
    }

    public void setAnswererID(String answererID) {
        this.answererID = answererID;
    }

    public Date getQuestiontime() {
        return questiontime;
    }

    public void setQuestiontime(Date questiontime) {
        this.questiontime = questiontime;
    }

    public Date getAnswertime() {
        return answertime;
    }

    public void setAnswertime(Date answertime) {
        this.answertime = answertime;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }
}
