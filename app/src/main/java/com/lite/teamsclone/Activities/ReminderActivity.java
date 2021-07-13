package com.lite.teamsclone.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lite.teamsclone.Adapters.EmailListAdapter;
import com.lite.teamsclone.Models.Meeting;
import com.lite.teamsclone.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Activity to schedule a meeting
 **/


public class ReminderActivity extends AppCompatActivity {

    TextInputEditText eventNameEt, eventDescEt, meetingIDEt, recipientEmailET;
    ExtendedFloatingActionButton addEventBtn;

    ExtendedFloatingActionButton btnDatePicker, btnTimePicker;

    private int mYear, mMonth, mDay, mHour, mMinute;
    String sDate1 = "";

    List<String> emailIdsList;
    RecyclerView emailListRV;
    EmailListAdapter adapter;
    FloatingActionButton addEmailButton;

    DatabaseReference scheduledMeetingsRef;

    FirebaseUser mCurrentUser;

    FirebaseFirestore firestoreDb;

    String date, time = "";

    SwitchMaterial calendarSwitch;

    boolean addToCalendar = false;

    long epoch;

    String uniqueMeetingId = "";
    TextView generateUniqueButton;

    TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        initUI();

        addEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (uniqueMeetingId.isEmpty()) {

                    errorTv.setText("Add a meeting ID");
                    errorTv.startAnimation(shakeError());

                } else {
                    addEvent();
                }
            }
        });

        addEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEmailToRV();
                adapter.notifyDataSetChanged();
            }
        });

        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });

        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sDate1.isEmpty()) {
                    errorTv.setText("Select a date fist!");
                    errorTv.startAnimation(shakeError());
                } else {
                    pickTime();
                }
            }
        });


        calendarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                addToCalendar = isChecked;
            }
        });


        //generate random ID
        generateUniqueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String currentTimestamp = String.valueOf(System.currentTimeMillis());

                Random random = new Random();

                char randomizedCharacter1 = (char) (random.nextInt(26) + 'a');
                char randomizedCharacter2 = (char) (random.nextInt(26) + 'a');

                uniqueMeetingId = String.valueOf(randomizedCharacter1) + String.valueOf(randomizedCharacter2) + currentTimestamp.substring(currentTimestamp.length() - 4);
                meetingIDEt.setText(uniqueMeetingId);
            }
        });

    }///oncreate ends here


    private void addEmailToRV() {

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (!recipientEmailET.getText().toString().isEmpty()) {

            if (!recipientEmailET.getText().toString().trim().matches(emailPattern)) {
                errorTv.setText("Invalid email address");
                errorTv.startAnimation(shakeError());
            } else {
                showToast("adding to the list");
                emailIdsList.add(recipientEmailET.getText().toString());
                recipientEmailET.setText("");
            }
        } else {
            errorTv.setText("Email can't be empty");
            errorTv.startAnimation(shakeError());
        }
    }

    private void addEvent() {

        StringBuilder emailIntent = new StringBuilder();

        for (String s : emailIdsList) {
            emailIntent.append(s + ", ");
        }

        if (!eventNameEt.getText().toString().isEmpty() &&
                !eventDescEt.getText().toString().isEmpty() &&
                !emailIntent.toString().isEmpty() &&
                !time.isEmpty()) {

            try {
                Date date1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(sDate1);
                epoch = date1.getTime();

            } catch (ParseException e) {
                e.printStackTrace();
            }


            if (addToCalendar) {

                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.putExtra(CalendarContract.Events.TITLE, eventNameEt.getText().toString());
                intent.putExtra(CalendarContract.Events.DESCRIPTION, eventDescEt.getText().toString());
                intent.putExtra(CalendarContract.Events.ALL_DAY, true);
                intent.putExtra(Intent.EXTRA_EMAIL, emailIntent.toString());

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);

                } else {
                    errorTv.setText("There is no app that supports this action");
                    errorTv.startAnimation(shakeError());
                }
            }

            Meeting meeting = new Meeting(mCurrentUser.getEmail(), eventNameEt.getText().toString(), uniqueMeetingId, emailIdsList, String.valueOf(epoch), eventDescEt.getText().toString());

            for (String s : emailIdsList) {
                firestoreDb.collection(s).document(uniqueMeetingId).set(meeting);
            }

            firestoreDb.collection(mCurrentUser.getEmail()).document(uniqueMeetingId).set(meeting);

            firestoreDb.collection("meetings").document(uniqueMeetingId).set(meeting)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            errorTv.setTextColor(Color.GREEN);
                            errorTv.setText("Meeting created! Added to database");
                            errorTv.startAnimation(animation());

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent i = new Intent(ReminderActivity.this, DashboardActivity.class);
                                    startActivity(i);
                                    finish();
                                }
                            }, 5000);

                        }
                    });

        } else {
            errorTv.setText("Fields cannot be empty!");
            errorTv.startAnimation(shakeError());
        }
    }


    //view time dialog
    private void pickTime() {

        time = "";

        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(ReminderActivity.this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        btnTimePicker.setText(hourOfDay + ":" + minute);

                        //HH:mm:ss

                        time = hourOfDay + ":" + minute + ":00";
                        sDate1 = sDate1 + time;
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }


    //view Date dialog
    private void pickDate() {
        sDate1 = "";

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(ReminderActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        btnDatePicker.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        sDate1 = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year + " ";
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void showToast(String s) {
        Toast.makeText(ReminderActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void initUI() {

        eventNameEt = findViewById(R.id.eventNameEt);
        eventDescEt = findViewById(R.id.eventDescEt);
        addEventBtn = findViewById(R.id.addEventButton);

        addEmailButton = findViewById(R.id.addEmailButton);
        recipientEmailET = findViewById(R.id.recipientEmailET);
        emailIdsList = new ArrayList<>();
        emailListRV = findViewById(R.id.emailListRV);
        emailListRV.setLayoutManager(new LinearLayoutManager(this));

        btnDatePicker = findViewById(R.id.dateBtn);
        btnTimePicker = findViewById(R.id.timeBtn);


        adapter = new EmailListAdapter(ReminderActivity.this, emailIdsList);

        emailListRV.setAdapter(adapter);

        scheduledMeetingsRef = FirebaseDatabase.getInstance().getReference().child("scheduled-meetings");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreDb = FirebaseFirestore.getInstance();

        calendarSwitch = findViewById(R.id.calendarSwitch);

        meetingIDEt = findViewById(R.id.meetingIDEt);
        generateUniqueButton = findViewById(R.id.uniqueIDTV);

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