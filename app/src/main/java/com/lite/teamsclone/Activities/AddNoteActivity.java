package com.lite.teamsclone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lite.teamsclone.Models.Note;
import com.lite.teamsclone.R;

public class AddNoteActivity extends AppCompatActivity {

    TextInputEditText notesTitleEt, notesBodyEt;
    FloatingActionButton confirmFAB, deleteFAB;

    FirebaseUser mCurrentUser;
    DatabaseReference notesRef;
    long epoch;

    String title, body;

    Note noteIntent;

    String noteId;
    private String toastMessage;

    TextView errorTv;

    String meetingCodeFromIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        initUI();

        if (noteIntent != null){
            deleteFAB.setEnabled(true);
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
                    if (title.isEmpty()){
                        errorTv.setText("Title cannot be empty");
                        errorTv.startAnimation(shakeError());
                    }
                    else{
                        errorTv.setText("Body cannot be empty");
                        errorTv.startAnimation(shakeError());
                    }
                }

            }
        });

        deleteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNote();
            }
        });
    }

    private void deleteNote() {

        notesRef.child(noteId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showToast("Removed!");
                        startActivity(new Intent(AddNoteActivity.this, MyNotesActivity.class));
                        finish();
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
        meetingCodeFromIntent = getIntent().getStringExtra("meetingCode");

        if (meetingCodeFromIntent != null){
            notesTitleEt.setText(meetingCodeFromIntent);
        }

        deleteFAB = findViewById(R.id.deleteFAB);
        errorTv = findViewById(R.id.errorTV);

    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }

    private TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(7));
        return shake;
    }

    private AlphaAnimation animation() {
        AlphaAnimation greenAnim = new AlphaAnimation(0.3f, 1.0f);
        greenAnim.setDuration(500);
        return greenAnim;
    }
}