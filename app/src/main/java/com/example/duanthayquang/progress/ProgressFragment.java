package com.example.duanthayquang.progress;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duanthayquang.R;
import com.example.duanthayquang.auth.LoginActivity;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.Profile;
import com.example.duanthayquang.database.User;

public class ProgressFragment extends Fragment {

    private TextView tvUserEmail, tvTotalXp, tvLevelDesc, tvXpToNext;
    private TextView tvStatStreak, tvStatQuestions, tvStatStars;
    private ProgressBar progressXpMain;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserEmail = view.findViewById(R.id.tv_user_email);
        tvTotalXp = view.findViewById(R.id.tv_total_xp);
        tvLevelDesc = view.findViewById(R.id.tv_level_desc);
        tvXpToNext = view.findViewById(R.id.tv_xp_to_next);
        tvStatStreak = view.findViewById(R.id.tv_stat_streak);
        tvStatQuestions = view.findViewById(R.id.tv_stat_questions);
        tvStatStars = view.findViewById(R.id.tv_stat_stars);
        progressXpMain = view.findViewById(R.id.progress_xp_main);

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> logout());

        loadProgress();
    }

    private void loadProgress() {
        if (getContext() == null) return;

        long userId = requireContext()
                .getSharedPreferences(LoginActivity.PREFS_AUTH, LoginActivity.MODE_PRIVATE)
                .getLong(LoginActivity.KEY_USER_ID, -1);
        if (userId == -1) return;

        AppDatabase db = AppDatabase.getInstance(requireContext());
        User user = db.userDao().getById(userId);
        Profile profile = db.profileDao().getByUserId(userId);
        int questionCount = db.questionAnswerDao().getAllByUserId(userId).size();

        if (user != null) {
            tvUserEmail.setText(user.email);
        }

        if (profile != null) {
            int xp = profile.xp;
            tvTotalXp.setText(String.valueOf(xp));
            int level = Math.max(1, xp / 100);
            tvLevelDesc.setText("LEVEL " + level + " • MASTER SCHOLAR");
            
            int xpPct = Math.min(100, Math.round((xp % 1200) * 100f / 1200));
            tvXpToNext.setText(xpPct + "%");
            progressXpMain.setProgress(xpPct);
            
            tvStatStreak.setText(profile.streak + "D");
            tvStatQuestions.setText(String.valueOf(questionCount));
            // Count stars from question_answers table
            new Thread(() -> {
                int starCount = db.questionAnswerDao().getAllByUserId(userId).stream()
                        .filter(qa -> qa.starred).toArray().length;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> tvStatStars.setText(String.valueOf(starCount)));
                }
            }).start();
        }
    }

    private void logout() {
        requireContext().getSharedPreferences(LoginActivity.PREFS_AUTH, LoginActivity.MODE_PRIVATE)
                .edit()
                .remove(LoginActivity.KEY_USER_ID)
                .apply();
        
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}