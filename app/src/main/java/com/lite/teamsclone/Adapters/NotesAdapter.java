package com.lite.teamsclone.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lite.teamsclone.Activities.AddNoteActivity;
import com.lite.teamsclone.Models.Note;
import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder>{

    Context context;
    List<Note> notesList;

    public NotesAdapter(Context context, List<Note> notesList) {
        this.context = context;
        this.notesList = notesList;
    }


    @NonNull
    @NotNull
    @Override
    public NotesAdapter.NotesViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new NotesViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull NotesAdapter.NotesViewHolder holder, int position) {

        holder.setNote(notesList.get(position));

        Note noteIntent = notesList.get(position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddNoteActivity.class);
                intent.putExtra("noteIntent", noteIntent);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class NotesViewHolder extends RecyclerView.ViewHolder {

        TextView title, date, body;


        public NotesViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textTitle);
            body = itemView.findViewById(R.id.textBody);
            date = itemView.findViewById(R.id.dateTime);
        }

        public void setNote(Note note) {

            title.setText(note.getTitle());
            body.setText(note.getBody());

            String dateEpoch = new java.text.SimpleDateFormat("dd/MM/yyyy h:mm a").format(new java.util.Date (note.getEpoch()));
            date.setText(dateEpoch);

        }
    }
}
