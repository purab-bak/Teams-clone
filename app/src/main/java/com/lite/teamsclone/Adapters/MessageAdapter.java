package com.lite.teamsclone.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_new, parent, false);
        return new MessageAdapterViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull @NotNull MessageAdapterViewHolder holder, int position) {

        Message message = messageList.get(position);

        holder.senderNameTV.setText(message.getSenderName());
        holder.messageTV.setText(message.getMessage());

        String date = new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a").format(new java.util.Date (message.getEpoch()));
        holder.dateEt.setText(date);

        Glide.with(context).load(message.getImagrUrl()).into(holder.userImage);

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView senderNameTV, messageTV, dateEt;

        LinearLayout messageLayout;

        ImageView userImage;

        public MessageAdapterViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            senderNameTV = itemView.findViewById(R.id.senderNameItem);
            messageTV = itemView.findViewById(R.id.messageItem);
            dateEt = itemView.findViewById(R.id.messageDateEt);

            userImage = itemView.findViewById(R.id.userImage);

        }
    }
}
