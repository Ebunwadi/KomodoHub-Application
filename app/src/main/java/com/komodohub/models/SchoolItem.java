package com.komodohub.models;

public class SchoolItem {
    private int id;
    private String name;
    private String subscriptionStatus;
    private int studentCount;
    private int teacherCount;

    public SchoolItem(int id, String name, String subscriptionStatus, int studentCount, int teacherCount) {
        this.id = id;
        this.name = name;
        this.subscriptionStatus = subscriptionStatus;
        this.studentCount = studentCount;
        this.teacherCount = teacherCount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public int getTeacherCount() {
        return teacherCount;
    }
}
