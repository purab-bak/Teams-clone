package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lite.housepartynew.Adapters.EmailListAdapter;
import com.lite.housepartynew.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReminderActivity extends AppCompatActivity {

    EditText eventNameEt, eventLocationEt, eventDescEt;
    Button addEventBtn;


//    Button btnDatePicker, btnTimePicker;
//    EditText txtDate, txtTime;
//    private int mYear, mMonth, mDay, mHour, mMinute;
//    String sDate1="";

    List<String> emailIdsList;
    RecyclerView emailListRV;
    EmailListAdapter adapter;
    Button addEmailButton;
    EditText recipientEmailET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        initUI();

        addEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringBuilder emailIntent = new StringBuilder();

                for (String s : emailIdsList){
                    emailIntent.append(s+", ");
                }

                if (!eventNameEt.getText().toString().isEmpty() &&
                        !eventDescEt.getText().toString().isEmpty() &&
                        !eventLocationEt.getText().toString().isEmpty() && !emailIntent.toString().isEmpty()) {

                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setData(CalendarContract.Events.CONTENT_URI);
                    intent.putExtra(CalendarContract.Events.TITLE, eventNameEt.getText().toString());
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, eventLocationEt.getText().toString());
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, eventDescEt.getText().toString());
                    intent.putExtra(CalendarContract.Events.ALL_DAY, true);
                    intent.putExtra(Intent.EXTRA_EMAIL, emailIntent.toString());

                    if (intent.resolveActivity(getPackageManager()) != null){
                        startActivity(intent);
                    }
                    else {
                        showToast("There is no app that supports this action");
                    }


                }
                else {
                    showToast("Fields cannot be empty");
                }
            }
        });

        addEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                if (!recipientEmailET.getText().toString().isEmpty()){

                    if (!recipientEmailET.getText().toString().trim().matches(emailPattern)) {
                        Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        showToast("adding to the list");
                        emailIdsList.add(recipientEmailET.getText().toString());
                        recipientEmailET.setText("");
                    }
                }
                else{
                    showToast("Email can't be empty");
                }
            }
        });


    }

    private void showToast(String s) {
        Toast.makeText(ReminderActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void initUI() {

        eventNameEt = findViewById(R.id.eventNameEt);
        eventLocationEt = findViewById(R.id.eventLocationEt);
        eventDescEt = findViewById(R.id.eventDescEt);
        addEventBtn = findViewById(R.id.addEventButton);

//        btnDatePicker=(Button)findViewById(R.id.btn_date);
//        btnTimePicker=(Button)findViewById(R.id.btn_time);
//        txtDate=(EditText)findViewById(R.id.in_date);
//        txtTime=(EditText)findViewById(R.id.in_time);

        addEmailButton = findViewById(R.id.addEmailButton);
        recipientEmailET = findViewById(R.id.recipientEmailET);
        emailIdsList = new ArrayList<>();
        emailListRV = findViewById(R.id.emailListRV);
        emailListRV.setLayoutManager(new LinearLayoutManager(this));

        adapter = new EmailListAdapter(ReminderActivity.this, emailIdsList);

        emailListRV.setAdapter(adapter);

    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }

    /**Date time not working**/
    /*{
        try {
            Date date1=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(sDate1);
            txtDate.setText(date1.toString());

            long epoch = date1.getTime();
            txtDate.setText(String.valueOf(epoch));

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setData(CalendarContract.Events.CONTENT_URI);
            intent.putExtra(CalendarContract.Events.TITLE, eventNameEt.getText().toString());
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, eventLocationEt.getText().toString());
            intent.putExtra(CalendarContract.Events.DESCRIPTION, eventDescEt.getText().toString());
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, String.valueOf(epoch));
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, String.valueOf(epoch+200));

            if (intent.resolveActivity(getPackageManager()) != null){
                startActivity(intent);
            }
            else {
                showToast("There is no app that supports this action");
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

            btnDatePicker.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            sDate1="";

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

                            txtDate.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            sDate1 = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year+" ";
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();


        }
    });

        btnTimePicker.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (sDate1.isEmpty()){
                showToast("Select a date first");
            }
            else{

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

                                txtTime.setText(hourOfDay + ":" + minute);

                                //HH:mm:ss
                                sDate1=sDate1+hourOfDay+":"+minute+":00";
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();

            }

        }
    });*/
}