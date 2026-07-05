package com.example.entity;

public class Post {
    private Integer id;
    private String title;
    private String content;
    private String category;
    private Integer userId;
    private String userName;
    private String userAvatar;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private String isTop;
    private String isEssence;
    private String status;
    private String time;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getReplyCount() { return replyCount; }
    public void setReplyCount(Integer replyCount) { this.replyCount = replyCount; }
    public String getIsTop() { return isTop; }
    public void setIsTop(String isTop) { this.isTop = isTop; }
    public String getIsEssence() { return isEssence; }
    public void setIsEssence(String isEssence) { this.isEssence = isEssence; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}