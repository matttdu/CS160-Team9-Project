package edu.sjsu.android.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;


import androidx.annotation.Nullable;

public class SQLiteController extends SQLiteOpenHelper {

    private static SQLiteController sqLiteController;
    private static final String DB_NAME = "BinSight";
    private static final int DB_VER = 4;
    public static final String TABLE_USERS = "Users";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String TABLE_POSTS = "Posts";
    public static final String COL_POST_ID = "id";
    public static final String COL_POST_TITLE = "title";
    public static final String COL_POST_CONTENT = "content";
    public static final String COL_POST_AUTHOR = "author";
    public static final String TABLE_MARKERS = "Markers";
    public static final String COL_MARKER_ID = "id";
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_TYPE = "type";
    public static final String COL_UPVOTES = "upvotes";
    public static final String COL_DOWNVOTES = "downvotes";

    public SQLiteController(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public static SQLiteController dbInstance(Context context) {
        if (sqLiteController == null)
            sqLiteController = new SQLiteController(context);

        return sqLiteController;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // createUsers
        String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COL_PASSWORD + " TEXT NOT NULL" +
                ")";

        // createPosts
        String createPosts = "CREATE TABLE IF NOT EXISTS " + TABLE_POSTS + " (" +
                COL_POST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_POST_TITLE + " TEXT NOT NULL, " +
                COL_POST_CONTENT + " TEXT NOT NULL, " +
                COL_POST_AUTHOR + " TEXT NOT NULL" +
                ")";

        // createMarkers
        String createMarkers = "CREATE TABLE IF NOT EXISTS " + TABLE_MARKERS + " (" +
                COL_MARKER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LATITUDE + " DOUBLE NOT NULL, " +
                COL_LONGITUDE + " DOUBLE NOT NULL, " +
                COL_TYPE + " TEXT NOT NULL CHECK (" + COL_TYPE + " IN ('recycling', 'compost', 'trash'))," +
                COL_UPVOTES + " INT DEFAULT 0, " +
                COL_DOWNVOTES + " INT DEFAULT 0" +
                ")";


        sqLiteDatabase.execSQL(createUsers);
        sqLiteDatabase.execSQL(createPosts);
        sqLiteDatabase.execSQL(createMarkers);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS);
        onCreate(sqLiteDatabase);
    }

    // User Registration Method
    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE username = ? OR email = ?", new String[]{username, email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
    
        if (exists) return false;
    
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
    
        long result = db.insert("Users", null, values);
        return result != -1;
    }

    // User Login Method
    public boolean loginUser(String usernameOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE (username = ? OR email = ?) AND password = ?",
                new String[]{usernameOrEmail, usernameOrEmail, password});
        boolean success = cursor.getCount() > 0;
        cursor.close();
        return success;
    }

    // Add Post Method
    public boolean addPost(String title, String content, String author) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_POST_TITLE, title);
        values.put(COL_POST_CONTENT, content);
        values.put(COL_POST_AUTHOR, author);
        long result = db.insert(TABLE_POSTS, null, values);
        //db.close();
        return result != -1;
    }

    public Cursor getAllPosts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_POSTS + " ORDER BY " + COL_POST_ID + " DESC", null);
    }

    // Add marker to database
    public boolean addMarker(double lat, double longitude, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LATITUDE, lat);
        values.put(COL_LONGITUDE, longitude);
        values.put(COL_TYPE, type);
        long result = db.insert(TABLE_MARKERS, null, values);
        return result != -1;
    }

    // Get all markers in Marker table
    public Cursor getAllMarkers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MARKERS + " ORDER BY " + COL_MARKER_ID + " DESC", null);
    }
}
