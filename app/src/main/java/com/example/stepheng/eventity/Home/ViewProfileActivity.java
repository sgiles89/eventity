package com.example.stepheng.eventity.Home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.stepheng.eventity.MainActivity;
import com.example.stepheng.eventity.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity implements LeaveTeamDialog.NoticeDialogListener{
    @BindView(R.id.view_profile_img)
    CircleImageView profileImg;
    @BindView(R.id.view_profile_name_text)
    TextView profileName;
    @BindView(R.id.view_profile_team_text)
    TextView profileTeam;
    @BindView(R.id.view_profile_toolbar) android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.edit_img)ImageView editImg;
    @BindView(R.id.leave_team_button)Button leaveBtn;

    private FirebaseFirestore mFStore;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    private String user_id;
    private Uri profileImageURI = null;
    private String name;
    private String teamID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        ButterKnife.bind(this);
        name = "Loading...";
        Intent intent = getIntent();
        final String profile_id = intent.getExtras().getString("profile_id");
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        mFStore = FirebaseFirestore.getInstance();
        user_id = mAuth.getInstance().getUid();

        DocumentReference profileRef = mFStore.collection("Users").document(profile_id);
        //load profile image
        profileRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String image = documentSnapshot.getString("image");
                    String name = documentSnapshot.getString("name");
                    profileName.setText(name);
                    getSupportActionBar().setTitle(name);
                    Glide.with(ViewProfileActivity.this).load(image).into(profileImg);
                }
            }
        });

        DocumentReference teamRef = mFStore.collection("Users/"+profile_id+"/Membership").document("Membership");
        teamRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();

                    String teamName = documentSnapshot.getString("teamName");
                    String role = documentSnapshot.getString("role");
                    setTeamID(documentSnapshot.getString("teamID"));
                    String capitalisedrole = role.substring(0, 1).toUpperCase() + role.substring(1);
                    profileTeam.setText(capitalisedrole+" of "+teamName);
                } else {
                    profileTeam.setVisibility(View.GONE);
                }
            }
        });
        //user is not viewing their own profile
        if (!profile_id.equals(user_id)){
            editImg.setVisibility(View.INVISIBLE);
            leaveBtn.setVisibility(View.INVISIBLE);
        }

        profileImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if (ContextCompat.checkSelfPermission(ViewProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ViewProfileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(ViewProfileActivity.this);
                    }
                }
            }
        });

        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNoticeDialog();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                profileImageURI = result.getUri();
                profileImg.setImageURI(profileImageURI);

                StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");
                image_path.putFile(profileImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            storeFirestore(task);

                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(ViewProfileActivity.this, "(IMAGE Error): " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    private void storeFirestore(Task<UploadTask.TaskSnapshot> task) {

        Uri download_uri = task.getResult().getDownloadUrl();

        mFStore.collection("Users").document(user_id).update("image", download_uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ViewProfileActivity.this, "Profile Image Updated", Toast.LENGTH_LONG).show();
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(ViewProfileActivity.this, "(FIRESTORE Error): " + error, Toast.LENGTH_LONG).show();
                }
            }}
        );
    }

    public void showNoticeDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new LeaveTeamDialog();
        dialog.show(getSupportFragmentManager(), "LeaveTeamDialog");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button

        // Get a new write batch
        WriteBatch batch = mFStore.batch();
        //set references for team membership locations-
        DocumentReference deleteMemberRef = mFStore.collection("Teams/"+teamID+"/Members").document(user_id);
        DocumentReference deleteMembershipRef = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");

        //delete references
        batch.delete(deleteMemberRef);
        batch.delete(deleteMembershipRef);

        // Commit the batch
        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(ViewProfileActivity.this, "You have successfully left the team", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(ViewProfileActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else{
                    Toast.makeText(ViewProfileActivity.this, "You were unable to leave the team. Please try again later"+task.getException(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    private void setTeamID(String teamID){
        this.teamID = teamID;
    }
}