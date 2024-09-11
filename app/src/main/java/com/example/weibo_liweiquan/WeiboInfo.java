package com.example.weibo_liweiquan;

import java.util.List;

public class WeiboInfo {
    private Long id;
    private Long userId;
    private String username;
    private String phone;
    private String avatar;
    private String title;
    private String videoUrl;
    private String poster;
    private List<String> images;
    private int likeCount;
    private boolean likeFlag;
    private String createTime;

    // Getters and Setters

    public String getAvatar() {
        return avatar;
    }

    public List<String> getImages() {
        return images;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
    public String getPoster() {
        return poster;
    }

    public int getLikeCount() {
        return likeCount;
    }
    public boolean getLikeFlag(){
        return likeFlag;
    }

    public void setLikeFlag(boolean likeFlag) {
        this.likeFlag = likeFlag;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getId() {
        return id;
    }
}


class WeiboResponse {
    private int code;
    private String msg;
    private Data data;

    class Data {
        private List<WeiboInfo> records;
        private int total;
        private int size;
        private int current;
        private int pages;
        public List<WeiboInfo> getRecords() {
            return records;
        }
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Data getData() {
        return data;
    }
}