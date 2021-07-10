package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.lite.housepartynew.R;

public class PreviousMeetingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_meetings);
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}