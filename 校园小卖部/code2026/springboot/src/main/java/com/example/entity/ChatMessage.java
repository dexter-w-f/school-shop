package com.example.entity;

public class ChatMessage {
    private Integer id;
    private Integer userId;
    private String userName;
    private String question;
    private String answer;
    private String time;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
