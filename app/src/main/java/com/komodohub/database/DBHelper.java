package com.komodohub.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "KomodoHub.db";
    private static final int DATABASE_VERSION = 1;  // Incremented to 3 for new changes

    // Table Names
    public static final String TABLE_SCHOOL = "school";
    public static final String TABLE_STUDENT = "student";
    public static final String TABLE_TEACHER = "teacher";
    public static final String TABLE_REPORT = "report";
    public static final String TABLE_MESSAGES = "messages";

    // Common Columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // School Table Columns
    public static final String COLUMN_SCHOOL_NAME = "school_name";
    public static final String COLUMN_PRINCIPAL_NAME = "principal_name";
    public static final String COLUMN_STATUS = "status";

    // Student Table Columns (Added bio and assigned_notified)
    public static final String COLUMN_TEACHER_ID = "teacher_id";
    public static final String COLUMN_SCHOOL_ID = "school_id";
    public static final String COLUMN_NICKNAME = "nickname";
    public static final String COLUMN_PROFILE_PIC = "profile_pic";
    public static final String COLUMN_BIO = "bio";

    // Teacher Table Columns (Added school_id to track which school a teacher belongs to)
    public static final String COLUMN_TEACHER_SCHOOL_ID = "school_id";

    // Report Table Columns (Feedback Added)
    public static final String COLUMN_STUDENT_ID = "student_id";
    public static final String COLUMN_REPORT_DETAILS = "details";
    public static final String COLUMN_IMAGE_PATH = "image_path";  // For uploaded images
    public static final String COLUMN_FEEDBACK = "feedback";  // For teacher feedback

    // Messages Table Columns (Principal and Admin Communication)
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_FROM = "message_from";
    public static final String COLUMN_TO = "message_to";

    // SQL to create tables
    private static final String CREATE_TABLE_SCHOOL = "CREATE TABLE " + TABLE_SCHOOL + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SCHOOL_NAME + " TEXT, " +
            COLUMN_PRINCIPAL_NAME + " TEXT, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_EMAIL + " TEXT, " +
            COLUMN_STATUS + " TEXT);";

    private static final String CREATE_TABLE_STUDENT = "CREATE TABLE " + TABLE_STUDENT + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_EMAIL + " TEXT, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_NICKNAME + " TEXT, " +
            COLUMN_PROFILE_PIC + " TEXT, " +
            COLUMN_BIO + " TEXT, " +
            COLUMN_TEACHER_ID + " INTEGER, " +
            COLUMN_SCHOOL_ID + " INTEGER);";

    private static final String CREATE_TABLE_TEACHER = "CREATE TABLE " + TABLE_TEACHER + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_EMAIL + " TEXT, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_TEACHER_SCHOOL_ID + " INTEGER);";  // Added school_id to relate teacher to school

    private static final String CREATE_TABLE_REPORT = "CREATE TABLE " + TABLE_REPORT + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_STUDENT_ID + " INTEGER, " +
            COLUMN_REPORT_DETAILS + " TEXT, " +
            COLUMN_IMAGE_PATH + " TEXT, " +
            COLUMN_FEEDBACK + " TEXT);";

    private static final String CREATE_TABLE_MESSAGES = "CREATE TABLE " + TABLE_MESSAGES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MESSAGE + " TEXT, " +
            COLUMN_FROM + " TEXT, " +
            COLUMN_TO + " TEXT);";

    // Indexes for better performance
    private static final String CREATE_INDEX_STUDENT_TEACHER = "CREATE INDEX idx_student_teacher ON " + TABLE_STUDENT + " (" + COLUMN_TEACHER_ID + ");";
    private static final String CREATE_INDEX_REPORT_STUDENT = "CREATE INDEX idx_report_student ON " + TABLE_REPORT + " (" + COLUMN_STUDENT_ID + ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SCHOOL);
        db.execSQL(CREATE_TABLE_STUDENT);
        db.execSQL(CREATE_TABLE_TEACHER);
        db.execSQL(CREATE_TABLE_REPORT);
        db.execSQL(CREATE_TABLE_MESSAGES);

        // Add indexes
        db.execSQL(CREATE_INDEX_STUDENT_TEACHER);
        db.execSQL(CREATE_INDEX_REPORT_STUDENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHOOL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEACHER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }
}

