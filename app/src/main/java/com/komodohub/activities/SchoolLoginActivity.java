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

public class SchoolLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_login);

        // Initialize UI components
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Handle school login
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (loginSchool(email, password)) {
                // Redirect to role selection page on successful login
                startActivity(new Intent(SchoolLoginActivity.this, RoleSelectionActivity.class));
            } else {
                Toast.makeText(SchoolLoginActivity.this, "Login failed. Check your credentials.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validate school login credentials
    private boolean loginSchool(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_SCHOOL + " WHERE email = ? AND password = ? ", new String[]{email, password});
        return cursor.getCount() > 0;
    }
}
