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

public class StudentLoginActivity extends AppCompatActivity {

    private EditText studentEmailInput, studentPasswordInput;
    private Button studentLoginButton;
    private DBHelper dbHelper;
    private int studentId;  // Dynamic student ID after login

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        // Initialize UI components
        studentEmailInput = findViewById(R.id.studentEmailInput);
        studentPasswordInput = findViewById(R.id.studentPasswordInput);
        studentLoginButton = findViewById(R.id.studentLoginButton);

        dbHelper = new DBHelper(this);

        // Handle student login
        studentLoginButton.setOnClickListener(v -> {
            String email = studentEmailInput.getText().toString();
            String password = studentPasswordInput.getText().toString();

            // Authenticate student and fetch ID dynamically
            if (authenticateStudent(email, password)) {
                // Redirect to the student's dashboard, passing the student's ID
                Intent intent = new Intent(StudentLoginActivity.this, StudentDashboardActivity.class);
                intent.putExtra("student_id", studentId);  // Pass student ID
                startActivity(intent);
            } else {
                Toast.makeText(StudentLoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Authenticate student and fetch ID
    private boolean authenticateStudent(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id FROM " + DBHelper.TABLE_STUDENT + " WHERE email = ? AND password = ?", new String[]{email, password});

        if (cursor.moveToFirst()) {
            studentId = cursor.getInt(0);  // Fetch dynamic student ID
            return true;
        }
        return false;
    }
}
