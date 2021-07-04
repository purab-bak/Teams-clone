package com.lite.housepartynew.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lite.housepartynew.Models.Message;
import com.lite.housepartynew.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageAdapterViewHolder> {

    Context context;
    List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
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

        holder.senderNameTV.setText(message.getName());
        holder.messageTV.setText(message.getMessage());

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView senderNameTV, messageTV;
        public MessageAdapterViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            senderNameTV = itemView.findViewById(R.id.senderNameItem);
            messageTV = itemView.findViewById(R.id.messageItem);

        }
    }
}
