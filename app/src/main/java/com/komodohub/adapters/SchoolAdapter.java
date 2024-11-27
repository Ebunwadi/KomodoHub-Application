package com.komodohub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;
import com.komodohub.activities.DeveloperDashboardActivity;
import com.komodohub.models.SchoolItem;
import com.komodohub.activities.R;

import java.util.List;

public class SchoolAdapter extends ArrayAdapter<SchoolItem> {

    private Context context;
    private List<SchoolItem> schools;

    public SchoolAdapter(@NonNull Context context, @NonNull List<SchoolItem> objects) {
        super(context, 0, objects);
        this.context = context;
        this.schools = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.school_item, parent, false);
        }

        SchoolItem school = getItem(position);

        TextView schoolNameTextView = convertView.findViewById(R.id.schoolNameTextView);
        TextView subscriptionStatusTextView = convertView.findViewById(R.id.subscriptionStatusTextView);
        TextView studentCountTextView = convertView.findViewById(R.id.studentCountTextView);
        TextView teacherCountTextView = convertView.findViewById(R.id.teacherCountTextView);
        Button toggleButton = convertView.findViewById(R.id.toggleSubscriptionButton);

        schoolNameTextView.setText(school.getName());
        subscriptionStatusTextView.setText("Status: " + school.getSubscriptionStatus());
        studentCountTextView.setText("Students: " + school.getStudentCount());
        teacherCountTextView.setText("Teachers: " + school.getTeacherCount());

        // Handle subscription toggle
        toggleButton.setText(school.getSubscriptionStatus().equals("active") ? "Deactivate" : "Activate");
        toggleButton.setOnClickListener(v -> {
            if (context instanceof DeveloperDashboardActivity) {
                ((DeveloperDashboardActivity) context).toggleSubscription(school.getId(), school.getSubscriptionStatus());
            }
        });

        return convertView;
    }
}
