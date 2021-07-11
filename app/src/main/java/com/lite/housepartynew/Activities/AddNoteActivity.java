package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lite.housepartynew.Models.Note;
import com.lite.housepartynew.R;

public class AddNoteActivity extends AppCompatActivity {

    EditText notesTitleEt, notesBodyEt;
    FloatingActionButton confirmFAB;

    FirebaseUser mCurrentUser;
    DatabaseReference notesRef;
    long epoch;

    String title, body;

    Note noteIntent;

    String noteId;
    private String toastMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        initUI();

        if (noteIntent != null){
            notesTitleEt.setText(noteIntent.getTitle());
            notesBodyEt.setText(noteIntent.getBody());

            noteId = noteIntent.getNoteId();

            toastMessage = "Note Edited!";
        }
        else {
            noteId = notesRef.push().getKey();
            toastMessage = "Note Added!";

        }

        epoch = System.currentTimeMillis();

        confirmFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title = notesTitleEt.getText().toString();
                body  = notesBodyEt.getText().toString();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(body)){

                    saveToDatabase();
                }
                else {
                    showToast("Cannot be empty");
                }

            }
        });
    }

    private void saveToDatabase() {

        Note note = new Note(title, body, noteId, epoch);

        notesRef.child(noteId).setValue(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showToast(toastMessage);
                        startActivity(new Intent(AddNoteActivity.this, MyNotesActivity.class));
                        finish();
                    }
                });


    }

    private void showToast(String s) {
        Toast.makeText(AddNoteActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void initUI() {

        notesTitleEt = findViewById(R.id.noteTitleEt);
        notesBodyEt = findViewById(R.id.noteBodyEt);
        confirmFAB = findViewById(R.id.confirmFAB);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        notesRef = FirebaseDatabase.getInstance().getReference().child("user-notes").child(mCurrentUser.getUid());

        noteIntent = (Note) getIntent().getSerializableExtra("noteIntent");

    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}