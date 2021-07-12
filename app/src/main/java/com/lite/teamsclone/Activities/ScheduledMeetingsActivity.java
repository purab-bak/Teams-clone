package com.lite.teamsclone.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lite.teamsclone.Adapters.ScheduledMeetingsAdapter;
import com.lite.teamsclone.Models.Meeting;
import com.lite.teamsclone.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ScheduledMeetingsActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    FirebaseFirestore firestoreDb;

    //RV

    RecyclerView meetingsRV;
    ScheduledMeetingsAdapter adapter;

    List<Meeting> meetingsList;
    LottieAnimationView lottieAnimationView;
    TextView textError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_meetings);

        initUI();

        long epoch = System.currentTimeMillis();
        firestoreDb.collection(mCurrentUser.getEmail())
                .whereGreaterThan("timeUTC", String.valueOf(epoch))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    if (document.exists()) {
                                        lottieAnimationView.setVisibility(View.GONE);

                                        Meeting meeting = document.toObject(Meeting.class);
                                        meetingsList.add(meeting);
                                        adapter.notifyDataSetChanged();
                                    }

                                }
                            } else {
                                lottieAnimationView.setAnimation(R.raw.error);
                                lottieAnimationView.playAnimation();
                                textError.setVisibility(View.VISIBLE);
                            }

                        } else {
                            lottieAnimationView.setAnimation(R.raw.error);
                            lottieAnimationView.playAnimation();
                            textError.setText(task.getException().getMessage());
                            textError.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    private void initUI() {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreDb = FirebaseFirestore.getInstance();

        lottieAnimationView = findViewById(R.id.lottieAnim);
        textError = findViewById(R.id.textError);

        meetingsList = new ArrayList<>();
        meetingsRV = findViewById(R.id.meetingsRV);

        //Meeting meeting = new Meeting("a", "a", "a", null, "a", "a");
        //meetingsList.add(meeting);

        adapter = new ScheduledMeetingsAdapter(ScheduledMeetingsActivity.this, meetingsList);

        meetingsRV.setLayoutManager(new LinearLayoutManager(this));
        meetingsRV.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}