package edu.sjsu.android.myapplication;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import edu.sjsu.android.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration; // AppBarConfiguration
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        // appBarConfiguration
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_profile
        ).build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener((con, dest, args) -> {
            if (dest.getId() == R.id.navigation_login || dest.getId() == R.id.navigation_register || dest.getId() == R.id.aboutFragment) {
                navView.setVisibility(GONE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().hide();
            } else {
                navView.setVisibility(VISIBLE);
                if (getSupportActionBar() != null)
                    getSupportActionBar().show();
            }
            invalidateOptionsMenu();
        });

        SQLiteController dbCon = new SQLiteController(this);
        SQLiteDatabase db = null;
        try {
            db = dbCon.getWritableDatabase();
            // use the database here
        } finally {
            if (db != null && db.isOpen()) db.close();
            dbCon.close();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        if (item.getItemId() == R.id.action_about) {
            navController.navigate(R.id.aboutFragment);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        MenuItem about = menu.findItem(R.id.action_about);
        about.setVisible(navController.getCurrentDestination().getId() != R.id.aboutFragment);
        return super.onPrepareOptionsMenu(menu);
    }
}