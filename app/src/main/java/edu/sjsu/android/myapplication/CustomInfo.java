package edu.sjsu.android.myapplication;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomInfo extends MarkerInfoWindow {
    private TextView titleText;
    private TextView titleContent;
    private Button upvoteBtn;
    private Button downvoteBtn;

    public CustomInfo(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        titleText = mView.findViewById(R.id.marker_title);
        titleContent = mView.findViewById(R.id.marker_content);
        upvoteBtn = mView.findViewById(R.id.upvoteBtn);
        downvoteBtn = mView.findViewById(R.id.downvoteBtn);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOpen(Object item) {
        if (item instanceof Marker) {
            Marker marker = (Marker) item;
            titleText.setText("Marker Score");
            int id = Integer.parseInt(marker.getTitle());
            SQLiteController dbCon = new SQLiteController(mView.getContext());
            SQLiteDatabase db = dbCon.getWritableDatabase();

            String[] arg = {marker.getTitle()};
            String stmt = "SELECT * FROM Markers WHERE id = " + id;
            Cursor cursor = db.rawQuery(stmt, null);
            if (cursor.getCount() == 1) {
                cursor.moveToNext();
                titleContent.setText("Upvotes: " + cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteController.COL_UPVOTES))
                                   + "\nDownvotes: " + cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteController.COL_DOWNVOTES)));
            }
            else {
                cursor.moveToNext();
                titleContent.setText("Error: cannot find marker data");
            }
            cursor.close();

            // Retrieving logged-in username from SharedPreferences
            SharedPreferences prefs = mView.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String loggedInUser = prefs.getString("loggedInUser", "Anonymous"); // fallback if missing

            String stmt2 = "SELECT * FROM " + SQLiteController.TABLE_MARKER_RATE + " WHERE " + SQLiteController.COL_USER_FOREIGN + " = '" + loggedInUser +
                    "' AND " + SQLiteController.COL_MARKER_FOREIGN + " = " + id;
            Cursor cursor2 = db.rawQuery(stmt2, null);
            if (cursor2.getCount() != 0) {
                cursor2.moveToNext();
                if (cursor2.getInt(cursor2.getColumnIndexOrThrow(SQLiteController.COL_UPVOTES)) == 1)
                    upvoteBtn.setEnabled(false);
                else if (cursor2.getInt(cursor2.getColumnIndexOrThrow(SQLiteController.COL_UPVOTES)) == 1)
                    downvoteBtn.setEnabled(false);
            }
            cursor2.close();

            upvoteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.beginTransaction();
                    try {
                        if (upvoteBtn.isEnabled() && downvoteBtn.isEnabled())
                            dbCon.addMarkerRating(true, false, loggedInUser, id);
                        else {
                            String stmt = "UPDATE " + SQLiteController.TABLE_MARKER_RATE + " SET " + SQLiteController.COL_UPVOTES + " = 1, " +
                                    SQLiteController.COL_DOWNVOTES + " = 0 WHERE " + SQLiteController.COL_USER_FOREIGN +
                                    " = '" + loggedInUser + "' AND " + SQLiteController.COL_MARKER_FOREIGN + " = " + id;
                            db.execSQL(stmt);
                        }
                        String stmt = "UPDATE " + SQLiteController.TABLE_MARKERS + " SET " +
                                SQLiteController.COL_UPVOTES + " = " + SQLiteController.COL_UPVOTES + " + 1 " + " WHERE id = " + id;
                        db.execSQL(stmt);
                        if (!downvoteBtn.isEnabled()) {
                            String stmt2 = "UPDATE " + SQLiteController.TABLE_MARKERS + " SET " +
                                    SQLiteController.COL_DOWNVOTES + " = " + SQLiteController.COL_DOWNVOTES + " - 1 " + " WHERE id = " + id;
                            db.execSQL(stmt2);
                            downvoteBtn.setEnabled(true);
                        }
                        upvoteBtn.setEnabled(false);
                        db.setTransactionSuccessful();
                    }
                    finally {
                        db.endTransaction();
                    }
                    onOpen(marker);
                }
            });

            downvoteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.beginTransaction();
                    try {
                        if (upvoteBtn.isEnabled() && downvoteBtn.isEnabled())
                            dbCon.addMarkerRating(true, false, loggedInUser, id);
                        else {
                            String stmt = "UPDATE " + SQLiteController.TABLE_MARKER_RATE + " SET " + SQLiteController.COL_UPVOTES + " = 0, " +
                                    SQLiteController.COL_DOWNVOTES + " = 1 WHERE " + SQLiteController.COL_USER_FOREIGN +
                                    " = '" + loggedInUser + "' AND " + SQLiteController.COL_MARKER_FOREIGN + " = " + id;
                            db.execSQL(stmt);
                        }
                        String stmt = "UPDATE " + SQLiteController.TABLE_MARKERS + " SET " +
                                SQLiteController.COL_DOWNVOTES + " = " + SQLiteController.COL_DOWNVOTES + " + 1 " + " WHERE id = " + id;
                        db.execSQL(stmt);
                        if (!upvoteBtn.isEnabled()) {
                            String stmt2 = "UPDATE " + SQLiteController.TABLE_MARKERS + " SET " +
                                    SQLiteController.COL_UPVOTES + " = " + SQLiteController.COL_UPVOTES + " - 1 " + " WHERE id = " + id;
                            db.execSQL(stmt2);
                            upvoteBtn.setEnabled(true);
                        }
                        downvoteBtn.setEnabled(false);
                        db.setTransactionSuccessful();
                    }
                    finally {
                        db.endTransaction();
                    }
                    onOpen(marker);
                }
            });
        }
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
