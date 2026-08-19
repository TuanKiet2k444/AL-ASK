package com.example.duanthayquang.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.duanthayquang.MainActivity;
import com.example.duanthayquang.R;
import com.example.duanthayquang.auth.LoginActivity;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.Profile;
import com.example.duanthayquang.database.ProfileDao;
import com.example.duanthayquang.database.ProfileDefaults;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnboardingActivity extends AppCompatActivity {

    private static final int STEP_LEVEL = 0;
    private static final int STEP_SUBJECTS = 1;
    private static final int STEP_STYLE = 2;

    private ProfileDao profileDao;
    private long userId;
    private Profile profile;
    private int currentStep = STEP_LEVEL;

    private String selectedLevel = ProfileDefaults.LEVEL_HIGH;
    private String selectedStyle = ProfileDefaults.STYLE_STEPWISE;
    private final Set<String> selectedSubjects = new HashSet<>(Arrays.asList(
            ProfileDefaults.SUBJECT_MATH,
            ProfileDefaults.SUBJECT_SCIENCE
    ));

    private View progress0, progress1, progress2;
    private TextView tvStepTitle, tvStepSubtitle;
    private LinearLayout stepLevel, stepSubjects, stepStyle;
    private MaterialButton btnBack, btnNext;

    private LinearLayout choiceLevelJunior, choiceLevelHigh, choiceLevelUni;
    private TextView choiceSubjectMath, choiceSubjectScience, choiceSubjectCode, choiceSubjectHistory, choiceSubjectLanguage, choiceSubjectOther;
    private LinearLayout choiceStyleShort, choiceStyleStepwise, choiceStyleDetailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userId = getSharedPreferences(LoginActivity.PREFS_AUTH, MODE_PRIVATE)
                .getLong(LoginActivity.KEY_USER_ID, -1);
        if (userId == -1) {
            finish();
            return;
        }

        profileDao = AppDatabase.getInstance(this).profileDao();
        ProfileDefaults.ensureExists(profileDao, userId);
        profile = profileDao.getByUserId(userId);

        if (profile.onboarded) {
            goToMain();
            return;
        }

        selectedLevel = profile.level;
        selectedStyle = profile.style;
        selectedSubjects.clear();
        for (String subject : profile.subjects.split(",")) {
            if (!subject.isEmpty()) {
                selectedSubjects.add(subject);
            }
        }

        bindViews();
        setupLevelChoices();
        setupSubjectChoices();
        setupStyleChoices();
        showStep(STEP_LEVEL);

        btnBack.setOnClickListener(v -> {
            if (currentStep > STEP_LEVEL) {
                showStep(currentStep - 1);
            }
        });

        btnNext.setOnClickListener(v -> onNext());
    }

    private void bindViews() {
        progress0 = findViewById(R.id.progress_0);
        progress1 = findViewById(R.id.progress_1);
        progress2 = findViewById(R.id.progress_2);
        tvStepTitle = findViewById(R.id.tv_step_title);
        tvStepSubtitle = findViewById(R.id.tv_step_subtitle);
        stepLevel = findViewById(R.id.step_level);
        stepSubjects = findViewById(R.id.step_subjects);
        stepStyle = findViewById(R.id.step_style);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);

        choiceLevelJunior = findViewById(R.id.choice_level_junior);
        choiceLevelHigh = findViewById(R.id.choice_level_high);
        choiceLevelUni = findViewById(R.id.choice_level_uni);

        choiceSubjectMath = findViewById(R.id.choice_subject_math);
        choiceSubjectScience = findViewById(R.id.choice_subject_science);
        choiceSubjectCode = findViewById(R.id.choice_subject_code);
        choiceSubjectHistory = findViewById(R.id.choice_subject_history);
        choiceSubjectLanguage = findViewById(R.id.choice_subject_language);
        choiceSubjectOther = findViewById(R.id.choice_subject_other);

        choiceStyleShort = findViewById(R.id.choice_style_short);
        choiceStyleStepwise = findViewById(R.id.choice_style_stepwise);
        choiceStyleDetailed = findViewById(R.id.choice_style_detailed);
    }

    private void setupLevelChoices() {
        choiceLevelJunior.setOnClickListener(v -> selectLevel(ProfileDefaults.LEVEL_JUNIOR));
        choiceLevelHigh.setOnClickListener(v -> selectLevel(ProfileDefaults.LEVEL_HIGH));
        choiceLevelUni.setOnClickListener(v -> selectLevel(ProfileDefaults.LEVEL_UNI));
        refreshLevelUi();
    }

    private void selectLevel(String level) {
        selectedLevel = level;
        refreshLevelUi();
    }

    private void refreshLevelUi() {
        setChoiceSelected(choiceLevelJunior, ProfileDefaults.LEVEL_JUNIOR.equals(selectedLevel));
        setChoiceSelected(choiceLevelHigh, ProfileDefaults.LEVEL_HIGH.equals(selectedLevel));
        setChoiceSelected(choiceLevelUni, ProfileDefaults.LEVEL_UNI.equals(selectedLevel));
    }

    private void setupSubjectChoices() {
        choiceSubjectMath.setOnClickListener(v -> toggleSubject(ProfileDefaults.SUBJECT_MATH, choiceSubjectMath));
        choiceSubjectScience.setOnClickListener(v -> toggleSubject(ProfileDefaults.SUBJECT_SCIENCE, choiceSubjectScience));
        choiceSubjectCode.setOnClickListener(v -> toggleSubject(ProfileDefaults.SUBJECT_CODE, choiceSubjectCode));
        choiceSubjectHistory.setOnClickListener(v -> toggleSubject(ProfileDefaults.SUBJECT_HISTORY, choiceSubjectHistory));
        choiceSubjectLanguage.setOnClickListener(v -> toggleSubject(ProfileDefaults.SUBJECT_LANGUAGE, choiceSubjectLanguage));
        choiceSubjectOther.setOnClickListener(v -> toggleSubject(ProfileDefaults.SUBJECT_OTHER, choiceSubjectOther));
        refreshSubjectUi();
    }

    private void toggleSubject(String subject, TextView view) {
        if (selectedSubjects.contains(subject)) {
            selectedSubjects.remove(subject);
        } else {
            selectedSubjects.add(subject);
        }
        refreshSubjectUi();
    }

    private void refreshSubjectUi() {
        setSubjectSelected(choiceSubjectMath, selectedSubjects.contains(ProfileDefaults.SUBJECT_MATH));
        setSubjectSelected(choiceSubjectScience, selectedSubjects.contains(ProfileDefaults.SUBJECT_SCIENCE));
        setSubjectSelected(choiceSubjectCode, selectedSubjects.contains(ProfileDefaults.SUBJECT_CODE));
        setSubjectSelected(choiceSubjectHistory, selectedSubjects.contains(ProfileDefaults.SUBJECT_HISTORY));
        setSubjectSelected(choiceSubjectLanguage, selectedSubjects.contains(ProfileDefaults.SUBJECT_LANGUAGE));
        setSubjectSelected(choiceSubjectOther, selectedSubjects.contains(ProfileDefaults.SUBJECT_OTHER));
    }

    private void setupStyleChoices() {
        choiceStyleShort.setOnClickListener(v -> selectStyle(ProfileDefaults.STYLE_SHORT));
        choiceStyleStepwise.setOnClickListener(v -> selectStyle(ProfileDefaults.STYLE_STEPWISE));
        choiceStyleDetailed.setOnClickListener(v -> selectStyle(ProfileDefaults.STYLE_DETAILED));
        refreshStyleUi();
    }

    private void selectStyle(String style) {
        selectedStyle = style;
        refreshStyleUi();
    }

    private void refreshStyleUi() {
        setChoiceSelected(choiceStyleShort, ProfileDefaults.STYLE_SHORT.equals(selectedStyle));
        setChoiceSelected(choiceStyleStepwise, ProfileDefaults.STYLE_STEPWISE.equals(selectedStyle));
        setChoiceSelected(choiceStyleDetailed, ProfileDefaults.STYLE_DETAILED.equals(selectedStyle));
    }

    private void setChoiceSelected(LinearLayout view, boolean selected) {
        view.setBackgroundResource(selected
                ? R.drawable.bg_onboarding_choice_selected
                : R.drawable.bg_onboarding_choice);
    }

    private void setSubjectSelected(TextView view, boolean selected) {
        view.setBackgroundResource(selected
                ? R.drawable.bg_onboarding_subject_selected
                : R.drawable.bg_onboarding_subject);
    }

    private void showStep(int step) {
        currentStep = step;

        stepLevel.setVisibility(step == STEP_LEVEL ? View.VISIBLE : View.GONE);
        stepSubjects.setVisibility(step == STEP_SUBJECTS ? View.VISIBLE : View.GONE);
        stepStyle.setVisibility(step == STEP_STYLE ? View.VISIBLE : View.GONE);

        int brand = ContextCompat.getColor(this, R.color.auth_brand);
        int muted = ContextCompat.getColor(this, R.color.auth_card);

        progress0.setBackgroundColor(step >= STEP_LEVEL ? brand : muted);
        progress1.setBackgroundColor(step >= STEP_SUBJECTS ? brand : muted);
        progress2.setBackgroundColor(step >= STEP_STYLE ? brand : muted);

        btnBack.setVisibility(step > STEP_LEVEL ? View.VISIBLE : View.GONE);

        if (step == STEP_LEVEL) {
            tvStepTitle.setText(R.string.onboarding_step_level_title);
            tvStepSubtitle.setText(R.string.onboarding_step_level_subtitle);
            btnNext.setText(R.string.onboarding_continue);
        } else if (step == STEP_SUBJECTS) {
            tvStepTitle.setText(R.string.onboarding_step_subjects_title);
            tvStepSubtitle.setText(R.string.onboarding_step_subjects_subtitle);
            btnNext.setText(R.string.onboarding_continue);
        } else {
            tvStepTitle.setText(R.string.onboarding_step_style_title);
            tvStepSubtitle.setText(R.string.onboarding_step_style_subtitle);
            btnNext.setText(R.string.onboarding_finish);
        }
    }

    private void onNext() {
        if (currentStep == STEP_SUBJECTS && selectedSubjects.isEmpty()) {
            Toast.makeText(this, R.string.onboarding_pick_subject, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentStep < STEP_STYLE) {
            showStep(currentStep + 1);
            return;
        }

        profile.level = selectedLevel;
        profile.style = selectedStyle;
        profile.subjects = joinSubjects(selectedSubjects);
        profile.onboarded = true;
        profileDao.update(profile);

        goToMain();
    }

    private static String joinSubjects(Set<String> subjects) {
        List<String> list = new ArrayList<>(subjects);
        return String.join(",", list);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}