package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lite.housepartynew.R;

public class MyNotesActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_notes);
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }

    public void onAddNoteClicked(View view) {
        startActivity(new Intent(MyNotesActivity.this, AddNoteActivity.class));
    }
}