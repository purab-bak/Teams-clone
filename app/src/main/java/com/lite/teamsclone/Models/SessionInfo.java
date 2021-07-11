package com.lite.teamsclone.Models;

public class SessionInfo {
    int userCount;
    String channelName;

    public SessionInfo(int userCount, String channelName) {
        this.userCount = userCount;
        this.channelName = channelName;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
