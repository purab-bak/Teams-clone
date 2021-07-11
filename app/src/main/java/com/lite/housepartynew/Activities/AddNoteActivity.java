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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        initUI();

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

        String noteId = notesRef.push().getKey();

        Note note = new Note(title, body, noteId, epoch);

        notesRef.child(noteId).setValue(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showToast("Note added successfully!");
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

    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}