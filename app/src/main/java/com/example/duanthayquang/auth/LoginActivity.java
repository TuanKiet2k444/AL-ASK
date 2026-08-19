package com.example.duanthayquang.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.duanthayquang.MainActivity;
import com.example.duanthayquang.R;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.ProfileDefaults;
import com.example.duanthayquang.database.User;
import com.example.duanthayquang.database.UserDao;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_AUTH = "PREFS_AUTH";
    public static final String KEY_USER_ID = "KEY_USER_ID";

    private EditText editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);

        findViewById(R.id.btn_login).setOnClickListener(v -> login());
        findViewById(R.id.tv_go_to_register).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void login() {
        String email = editEmail.getText().toString().trim().toLowerCase();
        String password = editPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.auth_error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.auth_error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, R.string.auth_error_password_short, Toast.LENGTH_SHORT).show();
            return;
        }

        UserDao userDao = AppDatabase.getInstance(this).userDao();
        User user = userDao.findByEmail(email);

        if (user == null || !password.equals(user.password)) {
            Toast.makeText(this, R.string.auth_error_invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        getSharedPreferences(PREFS_AUTH, MODE_PRIVATE)
                .edit()
                .putLong(KEY_USER_ID, user.id)
                .apply();

        ProfileDefaults.ensureExists(AppDatabase.getInstance(this).profileDao(), user.id);

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}