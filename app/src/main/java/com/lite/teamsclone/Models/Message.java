package com.lite.teamsclone.Models;

public class Message {
    public Message(){
    }

    String message, senderName, key;
    String imagrUrl;
    String senderUiD;
    long epoch;

    public Message(String message, String senderName, String imagrUrl, String senderUiD, long epoch) {
        this.message = message;
        this.senderName = senderName;
        this.imagrUrl = imagrUrl;
        this.senderUiD = senderUiD;
        this.epoch = epoch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getImagrUrl() {
        return imagrUrl;
    }

    public void setImagrUrl(String imagrUrl) {
        this.imagrUrl = imagrUrl;
    }

    public String getSenderUiD() {
        return senderUiD;
    }

    public void setSenderUiD(String senderUiD) {
        this.senderUiD = senderUiD;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }
}
