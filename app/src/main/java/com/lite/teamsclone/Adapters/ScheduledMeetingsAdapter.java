package com.lite.teamsclone.Adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lite.teamsclone.Activities.AddNoteActivity;
import com.lite.teamsclone.Activities.ChatActivity;
import com.lite.teamsclone.Activities.MainActivity;
import com.lite.teamsclone.Models.Meeting;
import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 Adapter and ViewHolder for displaying meetings
 **/

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_meeting_new, parent, false);
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

        holder.date.setText(date);
        holder.time.setText(time);

        holder.host.setText(meeting.getHostEmail());
        holder.desc.setText(meeting.getDescription());


        if (meeting.getParticipantsEmailList() != null){
            for (String s: meeting.getParticipantsEmailList()){
                participants += " " + s + "\n";

            }
            holder.participantsTv.setText(participants);

            if (epoch<System.currentTimeMillis()){
                holder.meetingCV.setCardBackgroundColor(context.getColor(R.color.green));
            }
            else {
                holder.meetingCV.setCardBackgroundColor(context.getColor(R.color.orangeCard));

            }


        }
        else {
            holder.participantsLL.setVisibility(View.GONE);
            holder.meetingCV.setCardBackgroundColor(context.getColor(R.color.darkGrey));

        }



        final boolean isExpanded = position==mExpandedPosition;
        holder.collapseLayout.setVisibility(isExpanded?View.VISIBLE:View.GONE);
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

        holder.notesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddNoteActivity.class);
                intent.putExtra("meetingCode", meeting.getMeetingId());
                context.startActivity(intent);
            }
        });

        holder.notesBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showToast("Add a note!");
                return true;
            }
        });

        holder.chatBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showToast("Open meeting chat!");
                return true;
            }
        });

        holder.joinBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showToast("Join the meeting!");
                return true;
            }
        });

        holder.copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Meeting Code", "Use the code "+meeting.getMeetingId() + " to join the meeting.");
                showToast("Meeting code copied to clipboard!");
                clipboard.setPrimaryClip(clip);
            }
        });

    }

    private void showToast(String s) {

        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
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

        ExtendedFloatingActionButton joinBtn;
        FloatingActionButton chatBtn, notesBtn;

        RelativeLayout collapseLayout;

        ImageView copyButton;

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

            collapseLayout = itemView.findViewById(R.id.collapseLayout);
            notesBtn = itemView.findViewById(R.id.notesButtonHolder);
            copyButton = itemView.findViewById(R.id.copyButton);

        }
    }
}
