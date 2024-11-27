package com.komodohub.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.komodohub.database.DBHelper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TableLayout.LayoutParams;

public class PrincipalActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendMessageButton;
    private TextView adminMessageView;
    private TableLayout studentTeacherTable; // TableLayout to display students, their assigned teachers, and teacher emails
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        // Initialize UI components
        messageInput = findViewById(R.id.messageInput);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        adminMessageView = findViewById(R.id.adminMessageView);
        studentTeacherTable = findViewById(R.id.studentTeacherTable); // For displaying students and teachers

        // Initialize DBHelper
        dbHelper = new DBHelper(this);

        // Load messages from school admin (if any)
        loadAdminMessages();

        // Load student-teacher assignments
        loadStudentTeacherAssignments();

        // Handle sending message to school admin
        sendMessageButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                sendMessageToAdmin(message);
                Toast.makeText(PrincipalActivity.this, "Message sent to School Admin", Toast.LENGTH_SHORT).show();
                messageInput.setText("");
            } else {
                Toast.makeText(PrincipalActivity.this, "Please enter a message", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load messages from the school admin
    private void loadAdminMessages() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_MESSAGES + " WHERE " + DBHelper.COLUMN_TO + " = 'principal'", null);
        if (cursor.moveToFirst()) {
            String message = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_MESSAGE));
            adminMessageView.setText("Message from School Admin: " + message);
        } else {
            adminMessageView.setText("No new messages");
        }
    }

    // Send a message to the school admin
    private void sendMessageToAdmin(String message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO " + DBHelper.TABLE_MESSAGES + " (message, message_from, message_to) VALUES ('" +
                message + "', 'principal', 'admin')");
    }

    // Load student-teacher assignments and display them in a table (with teacher email)
    private void loadStudentTeacherAssignments() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Updated SQL query to also retrieve the teacher's email
        Cursor cursor = db.rawQuery("SELECT student.name AS student_name, teacher.name AS teacher_name, teacher.email AS teacher_email " +
                "FROM " + DBHelper.TABLE_STUDENT + " AS student " +
                "INNER JOIN " + DBHelper.TABLE_TEACHER + " AS teacher ON student.teacher_id = teacher._id", null);

        // Clear any previous rows in the table
        studentTeacherTable.removeAllViews();

        // Add heading row to the table
        TableRow headingRow = new TableRow(this);
        headingRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView heading1 = new TextView(this);
        heading1.setText("Student Name");
        heading1.setTypeface(null, Typeface.BOLD);
        heading1.setPadding(10, 10, 10, 10);
        heading1.setGravity(Gravity.CENTER);

        TextView heading2 = new TextView(this);
        heading2.setText("Teacher Name");
        heading2.setTypeface(null, Typeface.BOLD);
        heading2.setPadding(10, 10, 10, 10);
        heading2.setGravity(Gravity.CENTER);

        TextView heading3 = new TextView(this);
        heading3.setText("Teacher Email");
        heading3.setTypeface(null, Typeface.BOLD);
        heading3.setPadding(10, 10, 10, 10);
        heading3.setGravity(Gravity.CENTER);

        headingRow.addView(heading1);
        headingRow.addView(heading2);
        headingRow.addView(heading3);
        studentTeacherTable.addView(headingRow);

        // Add rows with student-teacher assignments
        if (cursor.moveToFirst()) {
            do {
                String studentName = cursor.getString(cursor.getColumnIndex("student_name"));
                String teacherName = cursor.getString(cursor.getColumnIndex("teacher_name"));
                String teacherEmail = cursor.getString(cursor.getColumnIndex("teacher_email"));

                // Create a new row
                TableRow row = new TableRow(this);
                row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                // Create TextViews for student name, teacher name, and teacher email
                TextView studentTextView = new TextView(this);
                studentTextView.setText(studentName);
                studentTextView.setPadding(10, 10, 10, 10);
                studentTextView.setGravity(Gravity.CENTER);

                TextView teacherTextView = new TextView(this);
                teacherTextView.setText(teacherName);
                teacherTextView.setPadding(10, 10, 10, 10);
                teacherTextView.setGravity(Gravity.CENTER);

                TextView emailTextView = new TextView(this);
                emailTextView.setText(teacherEmail);
                emailTextView.setPadding(10, 10, 10, 10);
                emailTextView.setGravity(Gravity.CENTER);

                // Add the TextViews to the row
                row.addView(studentTextView);
                row.addView(teacherTextView);
                row.addView(emailTextView);

                // Add the row to the table
                studentTeacherTable.addView(row);
            } while (cursor.moveToNext());
        } else {
            // If no data is available, show a message in the table
            TableRow row = new TableRow(this);
            TextView emptyTextView = new TextView(this);
            emptyTextView.setText("No student-teacher assignments found.");
            emptyTextView.setPadding(10, 10, 10, 10);
            row.addView(emptyTextView);
            studentTeacherTable.addView(row);
        }
    }
}
