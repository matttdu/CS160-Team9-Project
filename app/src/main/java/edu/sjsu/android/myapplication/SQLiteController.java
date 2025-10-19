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
    private static final int DB_VER = 1;
    public static final String TABLE_USERS = "Users";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";

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
        String statement = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COL_USERNAME + " TEXT PRIMARY KEY, " +
                COL_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COL_PASSWORD + " TEXT NOT NULL" +
                ")";
        sqLiteDatabase.execSQL(statement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
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

}
