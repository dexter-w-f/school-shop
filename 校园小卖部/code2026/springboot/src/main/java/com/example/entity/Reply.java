package com.example.entity;

public class Reply {
    private Integer id;
    private Integer postId;
    private String content;
    private Integer userId;
    private String userName;
    private String userAvatar;
    private Integer parentId;
    private String time;
    private String parentName;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPostId() { return postId; }
    public void setPostId(Integer postId) { this.postId = postId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
}