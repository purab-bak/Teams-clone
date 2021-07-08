package com.lite.housepartynew.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.lite.housepartynew.R;

import java.io.File;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    ImageView profilePictureEdit;
    EditText nameEt;
    Button editConfirmButton;

    FirebaseUser mCurrentUser;
    private static final int PICK_IMAGE_REQUEST = 234;

    private Uri filePath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        nameEt = findViewById(R.id.accountNameEdit);
        profilePictureEdit = findViewById(R.id.accountPictureEdit);
        editConfirmButton = findViewById(R.id.accountEditConfirm);

        nameEt.setText(mCurrentUser.getDisplayName());


        if (mCurrentUser.getPhotoUrl() != null) {
            Glide.with(EditProfileActivity.this).load(mCurrentUser.getPhotoUrl()).into(profilePictureEdit);
            Toast.makeText(EditProfileActivity.this, mCurrentUser.getPhotoUrl().toString(), Toast.LENGTH_SHORT).show();
        }

        profilePictureEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });

        editConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (filePath != null){

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nameEt.getText().toString())
                            .setPhotoUri(filePath)
                            .build();

                    mCurrentUser.updateProfile(profileUpdates)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(EditProfileActivity.this, "Details updated", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(EditProfileActivity.this, DashboardActivity.class));
                                    finish();
                                }
                            });
                }
                else {

                    if (TextUtils.isEmpty(nameEt.getText().toString())){
                        Toast.makeText(EditProfileActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (nameEt.getText().toString().equals(mCurrentUser.getDisplayName())){
                            Toast.makeText(EditProfileActivity.this, "No changes detected", Toast.LENGTH_SHORT).show();

                        }
                        else {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nameEt.getText().toString())
                                    .build();


                            mCurrentUser.updateProfile(profileUpdates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(EditProfileActivity.this, "Name updated", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(EditProfileActivity.this, DashboardActivity.class));
                                            finish();
                                        }
                                    });
                        }
                    }

                }
            }
        });

    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profilePictureEdit.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onBackClicked(View view) {
        super.onBackPressed();
    }
}