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

public class RegisterActivity extends AppCompatActivity {

    private EditText editFullName, editEmail, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editFullName = findViewById(R.id.edit_fullname);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);

        findViewById(R.id.btn_register).setOnClickListener(v -> register());
        findViewById(R.id.tv_go_to_login).setOnClickListener(v -> finish());
    }

    private void register() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim().toLowerCase();
        String password = editPassword.getText().toString();

        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, R.string.auth_error_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

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
        if (userDao.findByEmail(email) != null) {
            Toast.makeText(this, R.string.auth_error_email_taken, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.fullName = fullName;
        user.email = email;
        user.password = password;

        try {
            long userId = userDao.insert(user);
            getSharedPreferences(LoginActivity.PREFS_AUTH, MODE_PRIVATE)
                    .edit()
                    .putLong(LoginActivity.KEY_USER_ID, userId)
                    .apply();

            ProfileDefaults.ensureExists(AppDatabase.getInstance(this).profileDao(), userId);

            Toast.makeText(this, R.string.auth_success_register, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}