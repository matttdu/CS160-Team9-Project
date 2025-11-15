package edu.sjsu.android.myapplication;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class CustomInfo extends MarkerInfoWindow {

    private SQLiteController dbCon;
    private TextView titleText;
    private TextView titleContent;

    public CustomInfo(int layoutResId, MapView mapView) {
        super(layoutResId, mapView);
        titleText = mView.findViewById(R.id.marker_title);
        titleContent = mView.findViewById(R.id.marker_content);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onOpen(Object item) {
        if (item instanceof Marker) {
            Marker marker = (Marker) item;
            titleText.setText("Marker Score");
            int id = Integer.parseInt(marker.getTitle());
            dbCon = new SQLiteController(mView.getContext());
            String[] arg = {marker.getTitle()};
            String stmt = "SELECT * FROM Markers WHERE id = " + id;
            Cursor cursor = dbCon.getReadableDatabase().rawQuery(stmt, null);
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
        }
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
