package com.lite.teamsclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lite.teamsclone.Adapters.NotesAdapter;
import com.lite.teamsclone.Models.Note;
import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MyNotesActivity extends AppCompatActivity {


    RecyclerView notesRV;

    List<Note> notesList;
    List<Note> filteredNotesList;
    NotesAdapter adapter;

    FirebaseUser mCurrentUser;
    DatabaseReference notesRef;

    SearchView searchView;

    LottieAnimationView lottieAnimationView;
    TextView textError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);

        initUI();

        getNotesFromDb();

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                filteredNotesList.clear();
//
//                for (Note n : notesList){
//                    if (n.getBody().toLowerCase().contains(query)){
//                        filteredNotesList.add(n);
//                    }
//                }
//
//                adapter = new NotesAdapter(MyNotesActivity.this,filteredNotesList);
//                notesRV.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
//
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//
//                return false;
//
//            }
//        });
    }

    private void getNotesFromDb() {

        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        lottieAnimationView.setVisibility(View.GONE);
                        Note note = dataSnapshot.getValue(Note.class);
                        notesList.add(note);
                        adapter.notifyDataSetChanged();
                    }
                }
                else {
                    lottieAnimationView.setAnimation(R.raw.error);
                    lottieAnimationView.playAnimation();
                    textError.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    private void initUI() {
        notesRV = findViewById(R.id.notesRV);

        searchView = findViewById(R.id.search_notes);

        notesRV.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        notesRef = FirebaseDatabase.getInstance().getReference().child("user-notes").child(mCurrentUser.getUid());

        notesList = new ArrayList<>();
        filteredNotesList = new ArrayList<>();
        adapter = new NotesAdapter(this, notesList);
        notesRV.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        lottieAnimationView = findViewById(R.id.lottieAnim);
        textError = findViewById(R.id.textError);
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }

    public void onAddNoteClicked(View view) {
        startActivity(new Intent(MyNotesActivity.this, AddNoteActivity.class));
        finish();
    }
}