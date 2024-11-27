package com.komodohub.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.komodohub.database.DBHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class StudentDashboardActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 3; // Code for gallery image selection
    private static final int REQUEST_EDIT_PROFILE = 2;
    private static final int REQUEST_PERMISSION = 100;

    private EditText reportDetailsInput;
    private Button submitReportButton, uploadImageButton, editProfileButton;
    private ImageView profileImageView, reportImageView;
    private TextView nicknameTextView, bioTextView;
    private ListView feedbackListView;
    private DBHelper dbHelper;
    private Uri reportImageUri;
    private String reportImagePath;
    private int studentId;

    private ArrayList<String> feedbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize UI components
        reportDetailsInput = findViewById(R.id.reportDetailsInput);
        submitReportButton = findViewById(R.id.submitReportButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        reportImageView = findViewById(R.id.reportImageView);
        profileImageView = findViewById(R.id.profileImageView);
        editProfileButton = findViewById(R.id.editProfileButton);
        nicknameTextView = findViewById(R.id.nicknameTextView);
        bioTextView = findViewById(R.id.bioTextView);
        feedbackListView = findViewById(R.id.feedbackListView);

        dbHelper = new DBHelper(this);
        feedbackList = new ArrayList<>();
        studentId = getIntent().getIntExtra("student_id", -1);  // Assuming student ID is passed from login

        // Load the student's profile details
        loadStudentProfile();

        // Load teacher feedback
        loadTeacherFeedback();

        // Handle navigating to Edit Profile
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, EditProfileActivity.class);
            intent.putExtra("student_id", studentId);  // Pass the student ID
            startActivityForResult(intent, REQUEST_EDIT_PROFILE);
        });

        // Handle image upload from gallery for report
        uploadImageButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
            } else {
                requestPermissions();
            }
        });

        // Handle report submission
        submitReportButton.setOnClickListener(v -> {
            String reportDetails = reportDetailsInput.getText().toString();
            if (!reportDetails.isEmpty()) {
                saveReport(reportDetails, reportImagePath);
                Toast.makeText(StudentDashboardActivity.this, "Report submitted", Toast.LENGTH_SHORT).show();
                reportDetailsInput.setText("");  // Clear the input field
                reportImageView.setImageDrawable(null);  // Clear the image
                reportImagePath = null;  // Reset image path
            } else {
                Toast.makeText(StudentDashboardActivity.this, "Please enter report details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check if storage permission is granted
    private boolean checkPermissions() {
        int readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return readStoragePermission == PackageManager.PERMISSION_GRANTED &&
                writeStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    // Request permissions for accessing storage
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_PERMISSION);
    }

    // Handle the result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the gallery
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
            } else {
                Toast.makeText(this, "Permission denied. Unable to access storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Load the student's profile from the database and display it
    private void loadStudentProfile() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nickname, bio, profile_pic FROM " + DBHelper.TABLE_STUDENT + " WHERE _id = ?", new String[]{String.valueOf(studentId)});
        if (cursor.moveToFirst()) {
            String nickname = cursor.getString(cursor.getColumnIndex("nickname"));
            String bio = cursor.getString(cursor.getColumnIndex("bio"));
            String profilePicPath = cursor.getString(cursor.getColumnIndex("profile_pic"));

            // Display nickname and bio
            nicknameTextView.setText(nickname);
            bioTextView.setText(bio);

            // Load profile image if available
            if (profilePicPath != null) {
                profileImageView.setImageURI(Uri.fromFile(new File(profilePicPath)));
            }
        }
        cursor.close();
    }

    // Save report details and image path to the database
    private void saveReport(String reportDetails, String imagePath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO " + DBHelper.TABLE_REPORT + " (details, student_id, image_path) VALUES (?, ?, ?)",
                new Object[]{reportDetails, studentId, imagePath});
    }

    // Save image to local storage and return its path
    private String saveImageToStorage(Uri imageUri) {
        try {
            // Get the input stream from the Uri (selected image)
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File storageDir = getExternalFilesDir(null);
            File imageFile = new File(storageDir, "report_image_" + System.currentTimeMillis() + ".jpg");

            // Save the image to the app's local storage
            OutputStream outputStream = new FileOutputStream(imageFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            // Return the absolute path of the saved image
            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Load feedback from teachers for the student
    private void loadTeacherFeedback() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT feedback FROM " + DBHelper.TABLE_REPORT + " WHERE student_id = ?", new String[]{String.valueOf(studentId)});

        feedbackList.clear();

        if (cursor.moveToFirst()) {
            do {
                String feedback = cursor.getString(0);
                feedbackList.add(feedback != null ? feedback : "No feedback available");
            } while (cursor.moveToNext());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, feedbackList);
        feedbackListView.setAdapter(adapter);
    }

    // Handle the result from image selection and edit profile activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();  // Get the image URI from gallery
            if (selectedImageUri != null) {
                reportImageView.setImageURI(selectedImageUri);  // Display the image in the ImageView
                reportImagePath = saveImageToStorage(selectedImageUri);  // Save image and get the path
            }
        } else if (requestCode == REQUEST_EDIT_PROFILE && resultCode == RESULT_OK) {
            // Get updated profile details from EditProfileActivity
            String updatedNickname = data.getStringExtra("updated_nickname");
            String updatedBio = data.getStringExtra("updated_bio");
            String updatedProfilePic = data.getStringExtra("updated_profile_pic");

            // Immediately update the profile details on the dashboard
            nicknameTextView.setText(updatedNickname);
            bioTextView.setText(updatedBio);

            if (updatedProfilePic != null) {
                profileImageView.setImageURI(Uri.fromFile(new File(updatedProfilePic)));
            }
        }
    }

    // Handle the result from the Edit Profile Activity
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == RESULT_OK) {
//            // Get updated profile details from EditProfileActivity
//            String updatedNickname = data.getStringExtra("updated_nickname");
//            String updatedBio = data.getStringExtra("updated_bio");
//            String updatedProfilePic = data.getStringExtra("updated_profile_pic");
//
//            // Immediately update the profile details on the dashboard
//            nicknameTextView.setText(updatedNickname);
//            bioTextView.setText(updatedBio);
//
//            if (updatedProfilePic != null) {
//                profileImageView.setImageURI(Uri.fromFile(new File(updatedProfilePic)));
//            }
//        }
//    }
}
