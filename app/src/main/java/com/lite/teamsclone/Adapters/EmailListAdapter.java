package com.lite.teamsclone.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmailListAdapter extends RecyclerView.Adapter<EmailListAdapter.EmailListViewHolder> {

    Context context;
    List<String> emailIdList;

    public EmailListAdapter(Context context, List<String> emailIdList) {
        this.context = context;
        this.emailIdList = emailIdList;
    }

    @NonNull
    @NotNull
    @Override
    public EmailListViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_email, parent, false);
        return new EmailListAdapter.EmailListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull EmailListViewHolder holder, int position) {

        String email = emailIdList.get(position);
        holder.emailTV.setText(email);

        holder.crossButtonTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailIdList.remove(position);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return emailIdList.size();
    }

    public class EmailListViewHolder extends RecyclerView.ViewHolder {

        TextView emailTV, crossButtonTV;


        public EmailListViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            emailTV = itemView.findViewById(R.id.emailHolderTV);
            crossButtonTV = itemView.findViewById(R.id.crossButtonHolder);
        }
    }
}
