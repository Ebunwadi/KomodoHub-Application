package com.komodohub.activities;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.komodohub.database.DBHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.util.ArrayList;

public class TeacherDashboardActivity extends AppCompatActivity {

    private ListView reportListView;
    private Button deleteReportButton, provideFeedbackButton;
    private ImageView reportImageView;  // ImageView to display report images
    private ArrayList<String> studentReports;
    private ArrayList<Integer> reportIds;
    private ArrayList<String> reportImagePaths;  // ArrayList to store image paths
    private DBHelper dbHelper;
    private int teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Teacher ID passed from login or previous activity
        teacherId = getIntent().getIntExtra("teacher_id", -1);

        reportListView = findViewById(R.id.reportListView);
        deleteReportButton = findViewById(R.id.deleteReportButton);
        provideFeedbackButton = findViewById(R.id.provideFeedbackButton);
        reportImageView = findViewById(R.id.reportImageView);  // ImageView for displaying images
        dbHelper = new DBHelper(this);
        studentReports = new ArrayList<>();
        reportIds = new ArrayList<>();
        reportImagePaths = new ArrayList<>();

        // Load student reports assigned to this teacher
        loadStudentReports();

        // Handle deletion of reports
        deleteReportButton.setOnClickListener(v -> {
            if (reportListView.getCheckedItemCount() > 0) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "No report selected for deletion", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle providing feedback
        provideFeedbackButton.setOnClickListener(v -> {
            int checkedItemPosition = reportListView.getCheckedItemPosition();
            if (checkedItemPosition != ListView.INVALID_POSITION) {
                showFeedbackDialog(checkedItemPosition);
            } else {
                Toast.makeText(this, "Please select a report to provide feedback", Toast.LENGTH_SHORT).show();
            }
        });

        // Display image when a report is selected
        reportListView.setOnItemClickListener((parent, view, position, id) -> {
            String imagePath = reportImagePaths.get(position);
            if (imagePath != null && !imagePath.isEmpty()) {
                reportImageView.setImageURI(Uri.fromFile(new File(imagePath)));  // Display image
            } else {
                reportImageView.setImageResource(android.R.color.transparent);  // No image, clear the ImageView
            }
        });
    }

    // Load student reports assigned to this teacher
    private void loadStudentReports() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT report._id, report.details, report.image_path, student.name FROM " + DBHelper.TABLE_REPORT +
                " AS report INNER JOIN " + DBHelper.TABLE_STUDENT + " AS student ON report.student_id = student._id " +
                "WHERE student.teacher_id = ?", new String[]{String.valueOf(teacherId)});

        studentReports.clear();
        reportIds.clear();
        reportImagePaths.clear();  // Clear previous paths

        if (cursor.moveToFirst()) {
            do {
                int reportId = cursor.getInt(cursor.getColumnIndex("_id"));
                String reportDetails = cursor.getString(cursor.getColumnIndex("details"));
                String studentName = cursor.getString(cursor.getColumnIndex("name"));
                String imagePath = cursor.getString(cursor.getColumnIndex("image_path"));

                studentReports.add(studentName + ": " + reportDetails);
                reportIds.add(reportId);
                reportImagePaths.add(imagePath);  // Store the image path (could be null)
            } while (cursor.moveToNext());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, studentReports);
        reportListView.setAdapter(adapter);
        reportListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    // Show confirmation dialog for deleting selected report
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Report");
        builder.setMessage("Are you sure you want to delete the selected report?");
        builder.setPositiveButton("Yes", (dialog, which) -> deleteSelectedReport());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    // Delete the selected report
    private void deleteSelectedReport() {
        int checkedItemPosition = reportListView.getCheckedItemPosition();
        if (checkedItemPosition != ListView.INVALID_POSITION) {
            int reportId = reportIds.get(checkedItemPosition);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " + DBHelper.TABLE_REPORT + " WHERE _id = " + reportId);
            loadStudentReports();  // Refresh the list
            Toast.makeText(this, "Report deleted", Toast.LENGTH_SHORT).show();
        }
    }

    // Show feedback dialog
    private void showFeedbackDialog(int checkedItemPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Provide Feedback");

        final EditText feedbackInput = new EditText(this);
        builder.setView(feedbackInput);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String feedback = feedbackInput.getText().toString();
            if (!feedback.isEmpty()) {
                int reportId = reportIds.get(checkedItemPosition);
                provideFeedback(reportId, feedback);
            } else {
                Toast.makeText(this, "Feedback cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Provide feedback for a specific report
    private void provideFeedback(int reportId, String feedback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DBHelper.TABLE_REPORT + " SET feedback = ? WHERE _id = ?", new Object[]{feedback, reportId});
        Toast.makeText(this, "Feedback submitted", Toast.LENGTH_SHORT).show();
    }
}
