package com.lite.teamsclone.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lite.teamsclone.R;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    ImageView profilePictureEdit;
    EditText nameEt;
    Button editConfirmButton;

    FirebaseUser mCurrentUser;
    private static final int PICK_IMAGE_REQUEST = 234;

    private Uri filePath;

    private StorageReference reference= FirebaseStorage.getInstance().getReference();

    String imageURL;


    TextView errorTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        nameEt = findViewById(R.id.accountNameEdit);
        profilePictureEdit = findViewById(R.id.accountPictureEdit);
        editConfirmButton = findViewById(R.id.accountEditConfirm);
        errorTv = findViewById(R.id.errorTV);


        nameEt.setText(mCurrentUser.getDisplayName());


        if (mCurrentUser.getPhotoUrl() != null) {
            Glide.with(EditProfileActivity.this).load(mCurrentUser.getPhotoUrl()).into(profilePictureEdit);
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

                    StorageReference fileref = reference.child(mCurrentUser.getUid());
                    fileref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    errorTv.setTextColor(Color.GREEN);
                                    errorTv.startAnimation(animation());
                                    errorTv.setText("Updating!");

                                    imageURL=uri.toString();

                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(nameEt.getText().toString())
                                            .setPhotoUri(Uri.parse(imageURL))
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
                            });

                        }
                    });


                }
                else {

                    if (TextUtils.isEmpty(nameEt.getText().toString())){

                        errorTv.startAnimation(shakeError());
                        errorTv.setText("Name cannot be empty");

                    }
                    else {
                        if (nameEt.getText().toString().equals(mCurrentUser.getDisplayName())){

                            errorTv.startAnimation(shakeError());
                            errorTv.setText("No changes detected");

                        }
                        else {

                            errorTv.setTextColor(Color.GREEN);
                            errorTv.startAnimation(animation());
                            errorTv.setText("Updating!");

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
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,2);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==2 && resultCode==RESULT_OK && data!=null){
            filePath=data.getData();
            profilePictureEdit.setImageURI(filePath);
        }
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

    private AlphaAnimation animation(){
        AlphaAnimation greenAnim = new AlphaAnimation(0.3f, 1.0f);
        greenAnim.setDuration(500);
        return greenAnim;
    }
}