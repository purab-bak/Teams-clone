package com.lite.teamsclone.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.lite.teamsclone.Models.Message;
import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageAdapterViewHolder> {


    Context context;
    List<Message> messageList;

    String currentUserName;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        currentUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

    }

    @NonNull
    @NotNull
    @Override
    public MessageAdapterViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageAdapterViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapterViewHolder holder, int position) {

        Message message = messageList.get(position);

        if (message.getName().equals(currentUserName)){
            holder.messageLayout.setBackgroundColor(Color.DKGRAY);

            holder.senderNameTV.setTextColor(Color.WHITE);
            holder.messageTV.setTextColor(Color.WHITE);


            holder.senderNameTV.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            holder.messageTV.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }


        holder.senderNameTV.setText(message.getName());
        holder.messageTV.setText(message.getMessage());

        String date = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new java.util.Date (message.getEpoch()));
        holder.dateEt.setText(date);


    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView senderNameTV, messageTV, dateEt;

        LinearLayout messageLayout;

        public MessageAdapterViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            senderNameTV = itemView.findViewById(R.id.senderNameItem);
            messageTV = itemView.findViewById(R.id.messageItem);
            messageLayout = itemView.findViewById(R.id.messageLayout);
            dateEt = itemView.findViewById(R.id.messageDateEt);

        }
    }
}
