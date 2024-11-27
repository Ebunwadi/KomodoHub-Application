package com.komodohub.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.komodohub.database.DBHelper;
import com.komodohub.adapters.SchoolAdapter;
import com.komodohub.models.SchoolItem;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class DeveloperDashboardActivity extends AppCompatActivity {

    private ListView schoolListView;
    private ArrayList<SchoolItem> schoolList;
    private SchoolAdapter schoolAdapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_dashboard);

        schoolListView = findViewById(R.id.schoolListView);
        dbHelper = new DBHelper(this);
        schoolList = new ArrayList<>();

        // Load school data
        loadSchoolData();
    }

    // Load schools from the database
    private void loadSchoolData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_SCHOOL, null);
        schoolList.clear();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper.COLUMN_ID));
                String schoolName = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_SCHOOL_NAME));
                String subscriptionStatus = cursor.getString(cursor.getColumnIndex(DBHelper.COLUMN_STATUS));

                // Get number of students
                Cursor studentCursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_STUDENT, null);
                studentCursor.moveToFirst();
                int studentCount = studentCursor.getInt(0);

                // Get number of teachers
                Cursor teacherCursor = db.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_TEACHER, null);
                teacherCursor.moveToFirst();
                int teacherCount = teacherCursor.getInt(0);

                // Add school data to the list
                schoolList.add(new SchoolItem(id, schoolName, subscriptionStatus, studentCount, teacherCount));
            } while (cursor.moveToNext());
        }

        // Set adapter to display the school list
        schoolAdapter = new SchoolAdapter(this, schoolList);
        schoolListView.setAdapter(schoolAdapter);
    }


    // Toggle subscription status for a school
    public void toggleSubscription(int schoolId, String currentStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String newStatus = currentStatus.equals("active") ? "inactive" : "active";
        db.execSQL("UPDATE " + DBHelper.TABLE_SCHOOL + " SET " + DBHelper.COLUMN_STATUS + " = '" + newStatus + "' WHERE " + DBHelper.COLUMN_ID + " = " + schoolId);
        Toast.makeText(this, "Subscription status updated to " + newStatus, Toast.LENGTH_SHORT).show();
        loadSchoolData();  // Reload the list to reflect changes
    }
}
