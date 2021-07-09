package com.lite.housepartynew.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lite.housepartynew.Models.Meeting;
import com.lite.housepartynew.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScheduledMeetingsAdapter extends RecyclerView.Adapter<ScheduledMeetingsAdapter.ScheduledMeetingsViewHolder> {

    Context context;
    List<Meeting> meetingList;

    public ScheduledMeetingsAdapter(Context context, List<Meeting> meetingList) {
        this.context = context;
        this.meetingList = meetingList;
    }

    @NonNull
    @NotNull
    @Override
    public ScheduledMeetingsAdapter.ScheduledMeetingsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.items_scheduled_meetings, parent, false);
        return new ScheduledMeetingsAdapter.ScheduledMeetingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ScheduledMeetingsAdapter.ScheduledMeetingsViewHolder holder, int position) {

        Meeting meeting = meetingList.get(position);

        holder.title.setText(meeting.getMeetingTitle());
        holder.meetingId.setText(meeting.getMeetingId());

    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    public class ScheduledMeetingsViewHolder extends RecyclerView.ViewHolder {

        TextView title, host, desc, date, time, meetingId;

        public ScheduledMeetingsViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            title = itemView.findViewById(R.id.meetingTitleHolder);
            host = itemView.findViewById(R.id.meetingHostHolder);
            desc = itemView.findViewById(R.id.meetingDescHolder);
            date = itemView.findViewById(R.id.meetingDateHolder);
            time = itemView.findViewById(R.id.meetingTimeHolder);
            meetingId = itemView.findViewById(R.id.meetingIdHolder);
        }
    }
}
