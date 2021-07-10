package com.lite.housepartynew.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.TransitionManager;

import com.google.android.material.button.MaterialButton;
import com.lite.housepartynew.Activities.ChatActivity;
import com.lite.housepartynew.Activities.JoinChannelActivity;
import com.lite.housepartynew.Activities.MainActivity;
import com.lite.housepartynew.Models.Meeting;
import com.lite.housepartynew.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScheduledMeetingsAdapter extends RecyclerView.Adapter<ScheduledMeetingsAdapter.ScheduledMeetingsViewHolder> {

    Context context;
    List<Meeting> meetingList;
    int mExpandedPosition, previousExpandedPosition;

    public ScheduledMeetingsAdapter(Context context, List<Meeting> meetingList) {
        this.context = context;
        this.meetingList = meetingList;
        mExpandedPosition = -1;
        previousExpandedPosition = -1;

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

        String participants = "";

        Meeting meeting = meetingList.get(position);

        holder.title.setText(meeting.getMeetingTitle());
        holder.meetingId.setText(meeting.getMeetingId());

        long epoch = Long.parseLong(meeting.getTimeUTC());

        String date = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date (epoch));
        String time = new java.text.SimpleDateFormat("HH:mm a").format(new java.util.Date (epoch));

        holder.date.append(" " + date);
        holder.time.append(" " + time);

        holder.host.append(" " +meeting.getHostEmail());
        holder.desc.append(" " + meeting.getDescription());
        for (String s: meeting.getParticipantsEmailList()){
            participants += " " + s + "\n";
        }

        holder.participantsTv.setText(participants);

        final boolean isExpanded = position==mExpandedPosition;
        holder.participantsLL.setVisibility(isExpanded?View.VISIBLE:View.GONE);
        holder.itemView.setActivated(isExpanded);

        if (isExpanded)
            previousExpandedPosition = position;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1:position;
                notifyItemChanged(previousExpandedPosition);
                notifyItemChanged(position);
            }
        });

        //onJoinCLicked

        holder.joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra("channelName", meeting.getMeetingId());
                context.startActivity(intent);
            }
        });

        holder.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("channelName", meeting.getMeetingId());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    public class ScheduledMeetingsViewHolder extends RecyclerView.ViewHolder {

        TextView title, host, desc, date, time, meetingId;

        LinearLayout participantsLL;
        TextView participantsTv;

        CardView meetingCV;

        MaterialButton joinBtn, chatBtn;

        public ScheduledMeetingsViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            title = itemView.findViewById(R.id.meetingTitleHolder);
            host = itemView.findViewById(R.id.meetingHostHolder);
            desc = itemView.findViewById(R.id.meetingDescHolder);
            date = itemView.findViewById(R.id.meetingDateHolder);
            time = itemView.findViewById(R.id.meetingTimeHolder);
            meetingId = itemView.findViewById(R.id.meetingIdHolder);
            participantsLL = itemView.findViewById(R.id.participantsLL);
            participantsTv = itemView.findViewById(R.id.participantsTvHolder);
            meetingCV = itemView.findViewById(R.id.meetingCV);
            joinBtn = itemView.findViewById(R.id.joinCallButtonHolder);
            chatBtn = itemView.findViewById(R.id.chatButtonHolder);

        }
    }
}
