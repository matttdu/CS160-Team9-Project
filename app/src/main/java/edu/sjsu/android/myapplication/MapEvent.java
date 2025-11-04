package edu.sjsu.android.myapplication;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapEvent implements MapEventsReceiver {

    private MapView map;
    private SQLiteController dbCon;
    private boolean listening = false;
    private String type = "";
    public MapEvent(MapView map) {
        this.map = map;
    }

    public void isListening(boolean value) {
        listening = value;

    }

    public void isListening(boolean value, String markerType) {
        listening = value;
        type = markerType;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        if (listening) {
            // Get the tapped location and instantiate as a marker object
            Marker marker = new Marker(map);
            marker.setPosition(p);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            if (type.equals("recycling")) {
                Bitmap bitmap = BitmapFactory.decodeResource(map.getResources(), R.drawable.recycling_icon);
                Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
                marker.setIcon(new BitmapDrawable(map.getResources(), bitmapResize));
            }
            else if (type.equals("compost")) {
                Bitmap bitmap = BitmapFactory.decodeResource(map.getResources(), R.drawable.compost_icon);
                Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
                marker.setIcon(new BitmapDrawable(map.getResources(), bitmapResize));
            }
            else {
                Bitmap bitmap = BitmapFactory.decodeResource(map.getResources(), R.drawable.trash_icon);
                Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
                marker.setIcon(new BitmapDrawable(map.getResources(), bitmapResize));
            }
            // Add the marker to the map
            map.getOverlays().add(marker);
            // Add the marker the the marker table in database
            dbCon = new SQLiteController(map.getContext());
            dbCon.addMarker(p.getLatitude(), p.getLongitude(), type);
            // Refresh map and return that operation was completed
            map.invalidate();
            return true;
        }
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}
