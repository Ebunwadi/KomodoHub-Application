package com.komodohub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.komodohub.database.DBHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TeacherLoginActivity extends AppCompatActivity {

    private EditText teacherEmailInput, teacherPasswordInput;
    private Button teacherLoginButton;
    private DBHelper dbHelper;
    private int teacherId;  // Dynamic teacher ID after login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_login);

        // Initialize UI components
        teacherEmailInput = findViewById(R.id.teacherEmailInput);
        teacherPasswordInput = findViewById(R.id.teacherPasswordInput);
        teacherLoginButton = findViewById(R.id.teacherLoginButton);

        dbHelper = new DBHelper(this);

        // Handle teacher login
        teacherLoginButton.setOnClickListener(v -> {
            String email = teacherEmailInput.getText().toString();
            String password = teacherPasswordInput.getText().toString();

            // Authenticate teacher and fetch ID dynamically
            if (authenticateTeacher(email, password)) {
                // Redirect to the teacher's dashboard, passing the teacher's ID
                Intent intent = new Intent(TeacherLoginActivity.this, TeacherDashboardActivity.class);
                intent.putExtra("teacher_id", teacherId);  // Pass teacher ID
                startActivity(intent);
            } else {
                Toast.makeText(TeacherLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Authenticate teacher and fetch ID
    private boolean authenticateTeacher(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id FROM " + DBHelper.TABLE_TEACHER + " WHERE email = ? AND password = ?", new String[]{email, password});

        if (cursor.moveToFirst()) {
            teacherId = cursor.getInt(0);  // Fetch dynamic teacher ID
            return true;
        }
        return false;
    }
}
