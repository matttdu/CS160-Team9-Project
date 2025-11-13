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
    private static final int DB_VER = 2;
    public static final String TABLE_USERS = "Users";
    public static final String COL_USERNAME = "username";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String TABLE_POSTS = "Posts";
    public static final String COL_POST_ID = "id";
    public static final String COL_POST_TITLE = "title";
    public static final String COL_POST_CONTENT = "content";
    public static final String COL_POST_AUTHOR = "author";
    public static final String TABLE_COMMENTS = "Comments";
    public static final String COL_COMMENT_ID = "comment_id";
    public static final String COL_COMMENT_POST_TITLE = "post_title";
    public static final String COL_COMMENT_AUTHOR = "author";
    public static final String COL_COMMENT_CONTENT = "content";

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
        String statement = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
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
        String createComments = "CREATE TABLE IF NOT EXISTS " + TABLE_COMMENTS + " (" +
                COL_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "post_id INTEGER NOT NULL, " +
                COL_COMMENT_AUTHOR + " TEXT NOT NULL, " +
                COL_COMMENT_CONTENT + " TEXT NOT NULL, " +
                "FOREIGN KEY(post_id) REFERENCES " + TABLE_POSTS + "(" + COL_POST_ID + ") ON DELETE CASCADE" +
                ")";

        sqLiteDatabase.execSQL(createComments);
        sqLiteDatabase.execSQL(statement);
        sqLiteDatabase.execSQL(createPosts);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
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

    // Update post by ID
    public boolean updatePostById(int postId, String newTitle, String newContent, String author) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_POST_TITLE, newTitle);
        values.put(COL_POST_CONTENT, newContent);

        int rows = db.update(TABLE_POSTS, values,
                COL_POST_ID + "=? AND " + COL_POST_AUTHOR + "=?",
                new String[]{String.valueOf(postId), author});

        db.close();
        return rows > 0;
    }

    // Delete post by ID
    public boolean deletePostById(int postId, String author) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete all comments for this post
        db.delete(TABLE_COMMENTS, "post_id=?", new String[]{String.valueOf(postId)});

        int rows = db.delete(TABLE_POSTS,
                COL_POST_ID + "=? AND " + COL_POST_AUTHOR + "=?",
                new String[]{String.valueOf(postId), author});
        db.close();
        return rows > 0;
    }

    // Add a comment
    public boolean addComment(int postId, String author, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("post_id", postId);
        values.put(COL_COMMENT_AUTHOR, author);
        values.put(COL_COMMENT_CONTENT, content);
        long result = db.insert(TABLE_COMMENTS, null, values);
        return result != -1;
    }

    // Get comments for a post
    public Cursor getComments(int postId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_COMMENTS,
                new String[]{COL_COMMENT_ID, COL_COMMENT_AUTHOR, COL_COMMENT_CONTENT},
                "post_id = ?",
                new String[]{String.valueOf(postId)},
                null, null, COL_COMMENT_ID + " ASC");
    }

    public Cursor getAllPosts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_POSTS + " ORDER BY " + COL_POST_ID + " DESC", null);
    }

}
