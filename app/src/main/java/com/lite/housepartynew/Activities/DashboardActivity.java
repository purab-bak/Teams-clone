package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.navigation.NavigationView;
import com.lite.housepartynew.R;

import org.jetbrains.annotations.NotNull;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{


    DrawerLayout drawerLayout;
    NavigationView navigationView;

    static final float END_SCALE = 0.8f;

    RelativeLayout homeRelativeLayout;
    ImageView menuIcon;

    Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initUI();

        window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        navigationDrawer();
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                    window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.LightestOrange));
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

    }

    private void navigationDrawer() {
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    private void initUI() {

        menuIcon = findViewById(R.id.menu_icon_dashboard_new);
        drawerLayout = findViewById(R.id.drawer_layout_new);
        navigationView = findViewById(R.id.navigation_view);
        homeRelativeLayout = findViewById(R.id.home_relative_layout);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        return false;
    }
}