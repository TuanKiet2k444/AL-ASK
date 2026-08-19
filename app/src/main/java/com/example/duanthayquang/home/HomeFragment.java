package com.example.duanthayquang.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duanthayquang.MainActivity;
import com.example.duanthayquang.R;
import com.example.duanthayquang.answer.AnswerActivity;
import com.example.duanthayquang.auth.LoginActivity;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.Profile;
import com.example.duanthayquang.database.QuestionAnswer;
import com.example.duanthayquang.database.QuestionAnswerDao;
import com.example.duanthayquang.database.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    private static final int XP_MAX = 1200;

    private TextView tvLevelBadge, tvStreak, tvXpLabel, tvXpPercent, tvGreeting;
    private ProgressBar progressXp;
    private MaterialButton btnAsk;

    private LinearLayout sectionRecent, cardRecent;
    private TextView tvViewAll, tvRecentQuestion, tvRecentSubject;
    private TextView tvCountMath, tvCountScience, tvCountCode, tvCountHistory;

    private long latestQaId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLevelBadge = view.findViewById(R.id.tv_level_badge);
        tvStreak = view.findViewById(R.id.tv_streak);
        tvXpLabel = view.findViewById(R.id.tv_xp_label);
        tvXpPercent = view.findViewById(R.id.tv_xp_percent);
        progressXp = view.findViewById(R.id.progress_xp);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        btnAsk = view.findViewById(R.id.btn_ask);

        sectionRecent = view.findViewById(R.id.section_recent);
        tvViewAll = view.findViewById(R.id.tv_view_all);
        cardRecent = view.findViewById(R.id.card_recent);
        tvRecentQuestion = view.findViewById(R.id.tv_recent_question);
        tvRecentSubject = view.findViewById(R.id.tv_recent_subject);

        tvCountMath = view.findViewById(R.id.tv_count_math);
        tvCountScience = view.findViewById(R.id.tv_count_science);
        tvCountCode = view.findViewById(R.id.tv_count_code);
        tvCountHistory = view.findViewById(R.id.tv_count_history);

        btnAsk.setOnClickListener(v -> selectTab(R.id.nav_ask));
        view.findViewById(R.id.btn_image).setOnClickListener(v -> 
                android.widget.Toast.makeText(requireContext(), "Image upload feature is coming soon", android.widget.Toast.LENGTH_SHORT).show());
        tvViewAll.setOnClickListener(v -> selectTab(R.id.nav_history));

        view.findViewById(R.id.card_subject_math).setOnClickListener(v -> navigateToHistory("math"));
        view.findViewById(R.id.card_subject_science).setOnClickListener(v -> navigateToHistory("science"));
        view.findViewById(R.id.card_subject_code).setOnClickListener(v -> navigateToHistory("code"));
        view.findViewById(R.id.card_subject_history).setOnClickListener(v -> navigateToHistory("history"));

        cardRecent.setOnClickListener(v -> {
            if (latestQaId != -1) {
                Intent intent = new Intent(requireContext(), AnswerActivity.class);
                intent.putExtra(AnswerActivity.EXTRA_QA_ID, latestQaId);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void loadDashboard() {
        if (getContext() == null) return;

        long userId = requireContext()
                .getSharedPreferences(LoginActivity.PREFS_AUTH, LoginActivity.MODE_PRIVATE)
                .getLong(LoginActivity.KEY_USER_ID, -1);
        if (userId == -1) return;

        AppDatabase db = AppDatabase.getInstance(requireContext());
        User user = db.userDao().getById(userId);
        Profile profile = db.profileDao().getByUserId(userId);
        QuestionAnswerDao qaDao = db.questionAnswerDao();

        String fullName = (user != null && user.fullName != null) ? user.fullName : "";
        int xp = (profile != null) ? profile.xp : 0;
        int streak = (profile != null) ? profile.streak : 0;

        int level = Math.max(1, xp / 100);
        int xpPct = Math.min(100, Math.round((xp * 100f) / XP_MAX));

        tvLevelBadge.setText(String.valueOf(level));
        tvStreak.setText(getString(R.string.home_streak_days, streak));
        tvXpLabel.setText(getString(R.string.home_xp_progress, xp, XP_MAX));
        tvXpPercent.setText(getString(R.string.home_xp_percent, xpPct));
        progressXp.setProgress(xpPct);
        tvGreeting.setText(getString(R.string.home_greeting_name, fullName.toUpperCase()));

        QuestionAnswer latest = qaDao.getLatestByUserId(userId);
        if (latest != null) {
            latestQaId = latest.id;
            sectionRecent.setVisibility(View.VISIBLE);
            tvRecentQuestion.setText(latest.question);
            tvRecentSubject.setText(subjectLabel(latest.subject));
        } else {
            latestQaId = -1;
            sectionRecent.setVisibility(View.GONE);
        }

        tvCountMath.setText(getString(R.string.home_subject_count, qaDao.countBySubject(userId, "math")));
        tvCountScience.setText(getString(R.string.home_subject_count, qaDao.countBySubject(userId, "science")));
        tvCountCode.setText(getString(R.string.home_subject_count, qaDao.countBySubject(userId, "code")));
        tvCountHistory.setText(getString(R.string.home_subject_count, qaDao.countBySubject(userId, "history")));
    }

    private void selectTab(int menuItemId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToTab(menuItemId, null);
        }
    }

    private void navigateToHistory(String subject) {
        if (getActivity() instanceof MainActivity) {
            Bundle args = new Bundle();
            args.putString("filter_subject", subject);
            ((MainActivity) getActivity()).navigateToTab(R.id.nav_history, args);
        }
    }

    private String subjectLabel(String subject) {
        if ("math".equals(subject)) return getString(R.string.subject_math);
        if ("science".equals(subject)) return getString(R.string.subject_science);
        if ("code".equals(subject)) return getString(R.string.subject_code);
        if ("history".equals(subject)) return getString(R.string.subject_history);
        if ("language".equals(subject)) return getString(R.string.subject_language);
        return getString(R.string.subject_other);
    }
}