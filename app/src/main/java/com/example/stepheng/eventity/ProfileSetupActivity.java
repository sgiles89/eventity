package com.example.stepheng.eventity;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetupActivity extends AppCompatActivity {
    //initialising Circle Image View
    private CircleImageView setupImage;
    private Uri profileImageURI = null;

    //initialising user ID and a variable to detect if the user profile image is changed
    private String user_id;
    private Boolean isChanged = false;

    //initialising the UI elements
    private EditText setupName;
    private Button setupBtn;
    private ProgressBar profileProgress;

    //initialising Firebase
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore mFStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        android.support.v7.widget.Toolbar profileSetupToolbar = findViewById(R.id.setup_toolbar);
        setSupportActionBar(profileSetupToolbar);
        getSupportActionBar().setTitle("Profile Setup");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        mFStore = FirebaseFirestore.getInstance();
        user_id = firebaseAuth.getInstance().getUid();

        setupImage = findViewById(R.id.profile_setup_image);
        setupName = findViewById(R.id.setup_name);
        setupBtn = findViewById(R.id.setup_btn);
        profileProgress = findViewById(R.id.profile_progress);

        profileProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        mFStore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    if(task.getResult().exists()) {

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        profileImageURI = Uri.parse(image);
                        setupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions().placeholder(R.drawable.defaultprofileimage);

                        Glide.with(ProfileSetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(setupImage);
                    }

                } else{
                    String error = task.getException().getMessage();
                    Toast.makeText(ProfileSetupActivity.this, "(FIRESTORE Retrieve Error): " + error, Toast.LENGTH_LONG).show();
                }
                profileProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = setupName.getText().toString();

                if (!TextUtils.isEmpty(user_name) && profileImageURI != null) {
                    profileProgress.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        user_id = firebaseAuth.getCurrentUser().getUid();

                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                        image_path.putFile(profileImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {

                                    storeFirestore(task, user_name);

                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(ProfileSetupActivity.this, "(IMAGE Error): " + error, Toast.LENGTH_LONG).show();

                                    profileProgress.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    } else {
                        storeFirestore(null, user_name);
                    }
                }
            }
    });
        setupImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(ProfileSetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(ProfileSetupActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(ProfileSetupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(ProfileSetupActivity.this);
                    }
                }
            }
        });

    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task, String user_name) {

        Uri download_uri;

        if (task !=null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = profileImageURI;
        }

        Map<String, String> userMap = new HashMap<>();

        userMap.put("name",user_name);
        userMap.put("image", download_uri.toString());

        mFStore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ProfileSetupActivity.this, "Profile settings updated", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(ProfileSetupActivity.this, "(FIRESTORE Error): " + error, Toast.LENGTH_LONG).show();
                }
            }}
        );
        profileProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                profileImageURI = result.getUri();
                setupImage.setImageURI(profileImageURI);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}
