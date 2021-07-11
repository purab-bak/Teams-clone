package com.lite.teamsclone.Models;

public class Message {
    public Message(){
    }

    String message, name, key;

    long epoch;

    public Message(String message, String name, long epoch) {
        this.message = message;
        this.name = name;
        this.epoch = epoch;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
