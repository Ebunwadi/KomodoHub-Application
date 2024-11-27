package com.komodohub.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.komodohub.database.DBHelper;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_SELECT = 1;
    private static final int REQUEST_PERMISSION = 100;

    private EditText nicknameInput, bioInput;
    private Button saveProfileButton, uploadProfileImageButton;
    private ImageView profileImageView;
    private DBHelper dbHelper;
    private Bitmap profileImageBitmap;
    private String profileImagePath;
    private int studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI components
        nicknameInput = findViewById(R.id.nicknameInput);
        bioInput = findViewById(R.id.bioInput);
        saveProfileButton = findViewById(R.id.saveProfileButton);
        uploadProfileImageButton = findViewById(R.id.uploadProfileImageButton);
        profileImageView = findViewById(R.id.profileImageView);

        dbHelper = new DBHelper(this);
        studentId = getIntent().getIntExtra("student_id", -1);  // Get student ID from intent

        // Handle profile image upload from gallery
        uploadProfileImageButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                openGallery();
            } else {
                requestPermissions();
            }
        });

        // Handle profile saving
        saveProfileButton.setOnClickListener(v -> saveProfile());
    }

    // Open gallery to select image
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    // Save nickname, bio, and profile picture to the database
    private void saveProfile() {
        String nickname = nicknameInput.getText().toString();
        String bio = bioInput.getText().toString();

        if (!nickname.isEmpty() && !bio.isEmpty()) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE " + DBHelper.TABLE_STUDENT + " SET nickname = ?, bio = ?, profile_pic = ? WHERE _id = ?",
                    new Object[]{nickname, bio, profileImagePath, studentId});
            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            // Return success and updated details to StudentDashboardActivity
            Intent returnIntent = new Intent();
            returnIntent.putExtra("updated_nickname", nickname);
            returnIntent.putExtra("updated_bio", bio);
            returnIntent.putExtra("updated_profile_pic", profileImagePath);
            setResult(RESULT_OK, returnIntent);
            finish();  // Return to previous activity
        } else {
            Toast.makeText(this, "Please enter both a nickname and bio", Toast.LENGTH_SHORT).show();
        }
    }

    // Save image to local storage and return its path
    private String saveImageToStorage(Bitmap imageBitmap) {
        File storageDir = getExternalFilesDir(null); // Use external storage for images
        File imageFile = new File(storageDir, "profile_image_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return imageFile.getAbsolutePath();
    }

    // Check if storage permission is granted
    private boolean checkPermissions() {
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    // Request permissions for accessing storage
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
    }

    // Handle the result from the image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData(); // Get the selected image URI
            try {
                profileImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                profileImageView.setImageBitmap(profileImageBitmap);  // Display the image
                profileImagePath = saveImageToStorage(profileImageBitmap);  // Save the image and get the path
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Handle the result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();  // Permission granted, open gallery
        } else {
            Toast.makeText(this, "Permission denied. Unable to access gallery.", Toast.LENGTH_SHORT).show();
        }
    }
}
