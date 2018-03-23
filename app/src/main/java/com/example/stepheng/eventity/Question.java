package com.example.stepheng.eventity;

public class Question {
    private String question;
    private String answer;
    private String asker;
    private String answerer;
    private String questiontime;
    private String answertime;
    private boolean isAnswered;
    private String eventID;

    public Question(){

    }

    public Question(String question, String answer, String asker, String answerer, String questiontime, String answertime, boolean isAnswered, String eventID) {
        this.question = question;
        this.answer = answer;
        this.asker = asker;
        this.answerer = answerer;
        this.questiontime = questiontime;
        this.answertime = answertime;
        this.isAnswered = isAnswered;
        this.eventID = eventID;
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

    public String getQuestiontime() {
        return questiontime;
    }

    public void setQuestiontime(String questiontime) {
        this.questiontime = questiontime;
    }

    public String getAnswertime() {
        return answertime;
    }

    public void setAnswertime(String answertime) {
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
}
