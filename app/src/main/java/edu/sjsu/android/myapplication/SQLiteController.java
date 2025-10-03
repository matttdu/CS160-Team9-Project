package edu.sjsu.android.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteController extends SQLiteOpenHelper {

    private static SQLiteController sqLiteController;
    private static final String DB_NAME = "BinSight";
    private static final int DB_VER = 1;

    public SQLiteController(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public static SQLiteController dbInstance(Context context) {
        if (sqLiteController == null)
            sqLiteController = new SQLiteController(context);

        return sqLiteController;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String statement = "CREATE TABLE Users (" +
                "username TEXT NOT NULL PRIMARY KEY," +
                "email TEXT NOT NULL," +
                "password TEXT NOT NULL" +
                ")";
        sqLiteDatabase.execSQL(statement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
