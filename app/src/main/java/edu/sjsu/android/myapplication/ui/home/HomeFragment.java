package edu.sjsu.android.myapplication.ui.home;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Objects;

import edu.sjsu.android.myapplication.CustomInfo;
import edu.sjsu.android.myapplication.MapEvent;
import edu.sjsu.android.myapplication.R;
import edu.sjsu.android.myapplication.SQLiteController;
import edu.sjsu.android.myapplication.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private MapView map;
    private SQLiteController dbCon;
    private FragmentHomeBinding binding;
    private FloatingActionButton addMarker;
    private FloatingActionButton recycleMarker;
    private FloatingActionButton compostMarker;
    private FloatingActionButton trashMarker;
    private boolean createMarkerToggle = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = requireActivity().getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        map = root.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);
        map.getController().setCenter(new GeoPoint(37.32, -121.88));

        // Create map click listener
        MapEvent receive = new MapEvent(map);
        MapEventsOverlay overlay = new MapEventsOverlay(receive);
        map.getOverlays().add(0, overlay);
        receive.isListening(false);

        // Create button click listener
        addMarker = root.findViewById(R.id.addMarkerButton);
        recycleMarker = root.findViewById(R.id.recycleButton);
        compostMarker = root.findViewById(R.id.compostButton);
        trashMarker = root.findViewById(R.id.trashButton);
        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createMarkerToggle) {
                    receive.isListening(false);
                    recycleMarker.setVisibility(View.GONE);
                    compostMarker.setVisibility(View.GONE);
                    trashMarker.setVisibility(View.GONE);
                }
                else {
                    recycleMarker.setVisibility(View.VISIBLE);
                    compostMarker.setVisibility(View.VISIBLE);
                    trashMarker.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Select marker type", Toast.LENGTH_SHORT).show();
                }
                createMarkerToggle = !createMarkerToggle;
            }
        });

        recycleMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Place marker", Toast.LENGTH_SHORT).show();
                receive.isListening(true, "recycling");
            }
        });

        compostMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Place marker", Toast.LENGTH_SHORT).show();
                receive.isListening(true, "compost");
            }
        });

        trashMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Place marker", Toast.LENGTH_SHORT).show();
                receive.isListening(true, "trash");
            }
        });

        // Request location permissions and create current user location marker
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

        MyLocationNewOverlay currLocation = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
        currLocation.enableMyLocation();
        map.getOverlays().add(currLocation);

        // Render existing markers
        dbCon = new SQLiteController(requireContext());
        Cursor cursor = dbCon.getAllMarkers();
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(SQLiteController.COL_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(SQLiteController.COL_LONGITUDE));
                String type = cursor.getString(cursor.getColumnIndexOrThrow(SQLiteController.COL_TYPE));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(SQLiteController.COL_MARKER_ID));

                GeoPoint point = new GeoPoint(latitude, longitude);
                Marker marker = new Marker(map);
                CustomInfo markerInfo = new CustomInfo(R.layout.custom_info_window, map);
                marker.setTitle(String.valueOf(id));
                marker.setInfoWindow(markerInfo);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                if (type.equals("recycling")) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.recycling_icon);
                    Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
                    marker.setIcon(new BitmapDrawable(getResources(), bitmapResize));
                }
                else if (type.equals("compost")) {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.compost_icon);
                    Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
                    marker.setIcon(new BitmapDrawable(getResources(), bitmapResize));
                }
                else {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.trash_icon);
                    Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
                    marker.setIcon(new BitmapDrawable(getResources(), bitmapResize));
                }
                map.getOverlays().add(0, marker);
            }
        }
        map.invalidate();
        cursor.close();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        map = requireView().findViewById(R.id.map);
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        map = requireView().findViewById(R.id.map);
        map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        map.onDetach();
    }
}
