package com.komodohub.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class RoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Button to navigate to Student Page
        Button studentButton = findViewById(R.id.studentButton);
        studentButton.setOnClickListener(v -> startActivity(new Intent(RoleSelectionActivity.this, StudentLoginActivity.class)));

        // Button to navigate to Teacher Page
        Button teacherButton = findViewById(R.id.teacherButton);
        teacherButton.setOnClickListener(v -> startActivity(new Intent(RoleSelectionActivity.this, TeacherLoginActivity.class)));

        // Button to navigate to Principal Page
        Button principalButton = findViewById(R.id.principalButton);
        principalButton.setOnClickListener(v -> startActivity(new Intent(RoleSelectionActivity.this, PrincipalActivity.class)));

        // Button to navigate to School Admin Page
        Button adminButton = findViewById(R.id.adminButton);
        adminButton.setOnClickListener(v -> startActivity(new Intent(RoleSelectionActivity.this, SchoolAdminActivity.class)));
    }
}
