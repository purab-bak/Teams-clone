package com.lite.housepartynew.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.lite.housepartynew.Fragments.EditProfileFragment;
import com.lite.housepartynew.Fragments.HomeFragment;
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
        //window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        navigationDrawer();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_dashboard, new HomeFragment()).commit();


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

        Fragment selectedFragment = new HomeFragment();

        if (item.getItemId() == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (item.getItemId()==R.id.nav_editProfle){
            selectedFragment = new EditProfileFragment();
            navigationView.setCheckedItem(R.id.menu_none);
            navigationView.setCheckedItem(R.id.nav_editProfle);

        }

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_dashboard, selectedFragment).commit();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finish();
        }
    }
}