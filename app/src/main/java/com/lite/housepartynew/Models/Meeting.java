package com.lite.housepartynew.Models;

import java.util.List;

public class Meeting {

    String hostEmail;
    String meetingTitle;
    String meetingId;
    List<String> participantsEmailList;
    String timeUTC;
    String description;

    public Meeting() {
    }

    public Meeting(String hostEmail, String meetingTitle, String meetingId, List<String> participantsEmailList, String timeUTC, String description) {
        this.hostEmail = hostEmail;
        this.meetingTitle = meetingTitle;
        this.meetingId = meetingId;
        this.participantsEmailList = participantsEmailList;
        this.timeUTC = timeUTC;
        this.description = description;
    }

    public String getHostEmail() {
        return hostEmail;
    }

    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
    }

    public String getMeetingTitle() {
        return meetingTitle;
    }

    public void setMeetingTitle(String meetingTitle) {
        this.meetingTitle = meetingTitle;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public List<String> getParticipantsEmailList() {
        return participantsEmailList;
    }

    public void setParticipantsEmailList(List<String> participantsEmailList) {
        this.participantsEmailList = participantsEmailList;
    }

    public String getTimeUTC() {
        return timeUTC;
    }

    public void setTimeUTC(String timeUTC) {
        this.timeUTC = timeUTC;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
