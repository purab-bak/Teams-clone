package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lite.housepartynew.R;

import org.jetbrains.annotations.NotNull;

public class ScheduledMeetingsActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    FirebaseFirestore firestoreDb;

    TextView tempTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_meetings);

        initUI();


        firestoreDb.collection(mCurrentUser.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                               tempTv.append(document.getId() + " => " + document.getData());
                            }
                        } else {
                            tempTv.append("Error getting documents.");
                        }
                    }
                });

    }

    private void initUI() {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestoreDb = FirebaseFirestore.getInstance();

        tempTv = findViewById(R.id.tempTv);
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}