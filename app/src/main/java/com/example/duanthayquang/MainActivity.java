package com.example.duanthayquang;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.duanthayquang.ask.AskFragment;
import com.example.duanthayquang.auth.LoginActivity;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.Profile;
import com.example.duanthayquang.database.ProfileDefaults;
import com.example.duanthayquang.history.HistoryFragment;
import com.example.duanthayquang.home.HomeFragment;
import com.example.duanthayquang.onboarding.OnboardingActivity;
import com.example.duanthayquang.progress.ProgressFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long userId = getSharedPreferences(LoginActivity.PREFS_AUTH, MODE_PRIVATE)
                .getLong(LoginActivity.KEY_USER_ID, -1);
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ProfileDefaults.ensureExists(AppDatabase.getInstance(this).profileDao(), userId);
        Profile profile = AppDatabase.getInstance(this).profileDao().getByUserId(userId);
        if (profile != null && !profile.onboarded) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            return switchFragment(id, null);
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    public void navigateToTab(int navId, Bundle args) {
        if (bottomNav != null) {
            switchFragment(navId, args);
            bottomNav.setSelectedItemId(navId);
        }
    }

    private boolean switchFragment(int id, Bundle args) {
        Fragment fragment;
        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_ask) {
            fragment = new AskFragment();
        } else if (id == R.id.nav_history) {
            fragment = new HistoryFragment();
        } else if (id == R.id.nav_progress) {
            fragment = new ProgressFragment();
        } else {
            return false;
        }

        if (args != null) {
            fragment.setArguments(args);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        return true;
    }
}