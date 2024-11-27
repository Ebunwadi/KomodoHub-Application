package com.komodohub.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.komodohub.database.DBHelper;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText schoolNameInput, principalNameInput, emailInput, passwordInput;
    private Button registerButton, loginButton, developerLoginButton;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        schoolNameInput = findViewById(R.id.schoolNameInput);
        principalNameInput = findViewById(R.id.principalNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);
        developerLoginButton = findViewById(R.id.developerLoginButton);

        // Initialize database helper
        dbHelper = new DBHelper(this);

        // Handle school/community registration
        registerButton.setOnClickListener(v -> {
            String schoolName = schoolNameInput.getText().toString();
            String principalName = principalNameInput.getText().toString();
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            // Check if fields are filled
            if (!schoolName.isEmpty() && !principalName.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Insert school and principal data into the database
                db.execSQL("INSERT INTO " + DBHelper.TABLE_SCHOOL + " (school_name, principal_name, password, email, status) VALUES ('" +
                        schoolName + "', '" + principalName + "', '" + password + "', '" + email + "', 'active')");

                Toast.makeText(MainActivity.this, "Registration successful. Kindly check your emails for further details.", Toast.LENGTH_LONG).show();

                // Clear fields
                schoolNameInput.setText("");
                principalNameInput.setText("");
                emailInput.setText("");
                passwordInput.setText("");

                // Redirect to login page
                startActivity(new Intent(MainActivity.this, SchoolLoginActivity.class));
            } else {
                Toast.makeText(MainActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            }
        });

        // Redirect to the school/community login page
        loginButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SchoolLoginActivity.class)));

        // Redirect to account developer login page
        developerLoginButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DeveloperLoginActivity.class)));
    }
}
