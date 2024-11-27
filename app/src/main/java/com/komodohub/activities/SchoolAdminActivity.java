package com.komodohub.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.komodohub.database.DBHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class SchoolAdminActivity extends AppCompatActivity {

    // UI elements for student and teacher registration
    private EditText studentNameInput, studentEmailInput, studentPasswordInput;
    private Spinner assignTeacherSpinner;
    private EditText teacherNameInput, teacherEmailInput, teacherPasswordInput;
    private Button registerStudentButton, registerTeacherButton;

    // UI elements for messaging with principal
    private EditText adminMessageInput;
    private Button sendMessageButton;
    private TextView principalMessageView;

    // UI elements for subscription management
    private ToggleButton subscriptionToggleButton;
    private TextView schoolDetailsTextView;

    private DBHelper dbHelper;
    private String schoolSubscriptionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_admin);

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Initialize UI elements for student and teacher registration
        studentNameInput = findViewById(R.id.studentNameInput);
        studentEmailInput = findViewById(R.id.studentEmailInput);
        studentPasswordInput = findViewById(R.id.studentPasswordInput);
        assignTeacherSpinner = findViewById(R.id.assignTeacherSpinner);
        teacherNameInput = findViewById(R.id.teacherNameInput);
        teacherEmailInput = findViewById(R.id.teacherEmailInput);
        teacherPasswordInput = findViewById(R.id.teacherPasswordInput);
        registerStudentButton = findViewById(R.id.registerStudentButton);
        registerTeacherButton = findViewById(R.id.registerTeacherButton);

        // Initialize UI elements for messaging
        adminMessageInput = findViewById(R.id.adminMessageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        principalMessageView = findViewById(R.id.principalMessageView);

        // Initialize UI elements for subscription management
        subscriptionToggleButton = findViewById(R.id.subscriptionToggleButton);
        schoolDetailsTextView = findViewById(R.id.schoolDetailsTextView);

        // Load school details
        loadSchoolDetails();

        // Populate teacher dropdown
        loadTeacherDropdown();

        // Handle student registration
        registerStudentButton.setOnClickListener(v -> registerStudent());

        // Handle teacher registration
        registerTeacherButton.setOnClickListener(v -> registerTeacher());

        // Handle sending messages to the principal
        sendMessageButton.setOnClickListener(v -> sendMessageToPrincipal());

        // Load messages from principal
        loadPrincipalMessages();

        // Handle subscription toggle
        subscriptionToggleButton.setOnCheckedChangeListener(this::onToggleChanged);
    }

    // Register a student and assign to a teacher
    private void registerStudent() {
        String studentName = studentNameInput.getText().toString();
        String studentEmail = studentEmailInput.getText().toString();
        String studentPassword = studentPasswordInput.getText().toString();
        String assignedTeacher = assignTeacherSpinner.getSelectedItem().toString();

        if (!studentName.isEmpty() && !studentEmail.isEmpty() && !studentPassword.isEmpty() && !assignedTeacher.equals("Assign to teacher")) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Get teacher ID based on selected teacher name
            Cursor teacherCursor = db.rawQuery("SELECT " + DBHelper.COLUMN_ID + " FROM " + DBHelper.TABLE_TEACHER + " WHERE " + DBHelper.COLUMN_NAME + " = ?", new String[]{assignedTeacher});
            if (teacherCursor.moveToFirst()) {
                int teacherId = teacherCursor.getInt(0);
                db.execSQL("INSERT INTO " + DBHelper.TABLE_STUDENT + " (name, email, password, teacher_id, school_id) VALUES ('" +
                        studentName + "', '" + studentEmail + "', '" + studentPassword + "', " + teacherId + ", 1)");
                Toast.makeText(this, "Student registered and assigned to teacher", Toast.LENGTH_SHORT).show();
                clearStudentFields();  // Clear text fields after registration
                loadSchoolDetails();
            } else {
                Toast.makeText(this, "Error: Teacher not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please fill all student details and select a teacher", Toast.LENGTH_SHORT).show();
        }
    }

    // Register a teacher
    private void registerTeacher() {
        String teacherName = teacherNameInput.getText().toString();
        String teacherEmail = teacherEmailInput.getText().toString();
        String teacherPassword = teacherPasswordInput.getText().toString();

        if (!teacherName.isEmpty() && !teacherEmail.isEmpty() && !teacherPassword.isEmpty()) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("INSERT INTO " + DBHelper.TABLE_TEACHER + " (name, email, password) VALUES ('" +
                    teacherName + "', '" + teacherEmail + "', '" + teacherPassword + "')");
            Toast.makeText(this, "Teacher registered", Toast.LENGTH_SHORT).show();
            clearTeacherFields();  // Clear text fields after registration
            loadTeacherDropdown();  // Refresh the teacher dropdown
            loadSchoolDetails();
        } else {
            Toast.makeText(this, "Please fill all teacher details", Toast.LENGTH_SHORT).show();
        }
    }

    // Clear student input fields after registration
    private void clearStudentFields() {
        studentNameInput.setText("");
        studentEmailInput.setText("");
        studentPasswordInput.setText("");
    }

    // Clear teacher input fields after registration
    private void clearTeacherFields() {
        teacherNameInput.setText("");
        teacherEmailInput.setText("");
        teacherPasswordInput.setText("");
    }

    // Load teacher dropdown with "Assign to teacher" as the first item
    private void loadTeacherDropdown() {
        ArrayList<String> teacherNames = new ArrayList<>();
        teacherNames.add("Assign to teacher"); // Placeholder

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DBHelper.COLUMN_NAME + " FROM " + DBHelper.TABLE_TEACHER, null);
        if (cursor.moveToFirst()) {
            do {
                teacherNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignTeacherSpinner.setAdapter(adapter);
    }

    // Load school details (subscription status, number of students, number of teachers)
    private void loadSchoolDetails() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor schoolCursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_SCHOOL + " WHERE _id = 1", null);
        if (schoolCursor.moveToFirst()) {
            String schoolName = schoolCursor.getString(schoolCursor.getColumnIndex(DBHelper.COLUMN_SCHOOL_NAME));
            String principalName = schoolCursor.getString(schoolCursor.getColumnIndex(DBHelper.COLUMN_PRINCIPAL_NAME));
            schoolSubscriptionStatus = schoolCursor.getString(schoolCursor.getColumnIndex(DBHelper.COLUMN_STATUS));

            // Set the toggle button based on the subscription status
            subscriptionToggleButton.setChecked(schoolSubscriptionStatus.equals("active"));

            // Get number of students
            Cursor studentCursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_STUDENT, null);
            studentCursor.moveToFirst();
            int studentCount = studentCursor.getInt(0);

            // Get number of teachers
            Cursor teacherCursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_TEACHER, null);
            teacherCursor.moveToFirst();
            int teacherCount = teacherCursor.getInt(0);

            // Display school details
            schoolDetailsTextView.setText("School: " + schoolName + "\nPrincipal: " + principalName + "\nSubscription: " + schoolSubscriptionStatus +
                    "\nNumber of Students: " + studentCount + "\nNumber of Teachers: " + teacherCount);
        }
    }

    // Send message to principal
    private void sendMessageToPrincipal() {
        String message = adminMessageInput.getText().toString();
        if (!message.isEmpty()) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("INSERT INTO " + DBHelper.TABLE_MESSAGES + " (message, message_from, message_to) VALUES ('" +
                    message + "', 'admin', 'principal')");
            Toast.makeText(this, "Message sent to Principal", Toast.LENGTH_SHORT).show();
            adminMessageInput.setText(""); // Clear message input field
        } else {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    // Load messages from the principal
    private void loadPrincipalMessages() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_MESSAGES + " WHERE " + DBHelper.COLUMN_TO + " = 'admin'", null);
        if (cursor.moveToFirst()) {
            String message = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MESSAGE));
            principalMessageView.setText("Message from Principal: " + message);
        } else {
            principalMessageView.setText("No new messages");
        }
    }

    // Toggle subscription status
    private void onToggleChanged(CompoundButton buttonView, boolean isChecked) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String newStatus = isChecked ? "active" : "inactive";
        db.execSQL("UPDATE " + DBHelper.TABLE_SCHOOL + " SET " + DBHelper.COLUMN_STATUS + " = '" + newStatus + "' WHERE _id = 1");
        loadSchoolDetails();
        Toast.makeText(this, "Subscription status updated to " + newStatus, Toast.LENGTH_SHORT).show();
    }
}