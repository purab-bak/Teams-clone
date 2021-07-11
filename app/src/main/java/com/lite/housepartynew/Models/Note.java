package com.lite.housepartynew.Models;

public class Note {

    String title, body, noteId;

    long epoch;

    public Note(String title, String body, String noteId, long epoch) {
        this.title = title;
        this.body = body;
        this.noteId = noteId;
        this.epoch = epoch;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public Note() {
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }
}
