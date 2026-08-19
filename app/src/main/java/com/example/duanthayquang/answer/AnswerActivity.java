package com.example.duanthayquang.answer;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.duanthayquang.R;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.QuestionAnswer;
import com.example.duanthayquang.database.QuestionAnswerDao;

import org.json.JSONArray;
import org.json.JSONObject;

public class AnswerActivity extends AppCompatActivity {

    public static final String EXTRA_QA_ID = "qa_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_answer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        long qaId = getIntent().getLongExtra(EXTRA_QA_ID, -1);
        if (qaId == -1) {
            Toast.makeText(this, R.string.answer_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        QuestionAnswerDao qaDao = AppDatabase.getInstance(this).questionAnswerDao();
        QuestionAnswer qa = qaDao.getById(qaId);
        if (qa == null) {
            Toast.makeText(this, R.string.answer_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btn_back);
        TextView tvSubject = findViewById(R.id.tv_subject);
        TextView tvQuestion = findViewById(R.id.tv_question);
        LinearLayout layoutSteps = findViewById(R.id.layout_steps);
        TextView tvFinalAnswer = findViewById(R.id.tv_final_answer);

        btnBack.setOnClickListener(v -> finish());
        tvSubject.setText(subjectLabel(qa.subject));
        tvQuestion.setText(qa.question);
        tvFinalAnswer.setText(qa.finalAnswer);

        try {
            JSONArray steps = new JSONArray(qa.steps);
            for (int i = 0; i < steps.length(); i++) {
                JSONObject step = steps.getJSONObject(i);
                addStepView(layoutSteps, i + 1, step);
            }
        } catch (Exception e) {
            TextView error = new TextView(this);
            error.setText(qa.steps);
            error.setTextColor(getColor(R.color.auth_foreground));
            layoutSteps.addView(error);
        }
    }

    private void addStepView(LinearLayout parent, int number, JSONObject step) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(16, 16, 16, 16);
        row.setBackground(getDrawable(R.drawable.bg_home_card));
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
        row.setLayoutParams(rowParams);

        TextView stepNumber = new TextView(this);
        stepNumber.setText(String.valueOf(number));
        stepNumber.setGravity(Gravity.CENTER);
        stepNumber.setTextColor(getColor(R.color.black));
        stepNumber.setBackground(getDrawable(R.drawable.bg_level_badge)); // Gold circle
        stepNumber.getBackground().setTint(getColor(R.color.toty_gold));
        int size = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams numberParams = new LinearLayout.LayoutParams(size, size);
        numberParams.setMargins(0, 4, (int) (12 * getResources().getDisplayMetrics().density), 0);
        stepNumber.setLayoutParams(numberParams);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        content.setLayoutParams(contentParams);

        String title = step.optString("title", "");
        if (!title.isEmpty()) {
            TextView tvTitle = new TextView(this);
            tvTitle.setText(title);
            tvTitle.setTextColor(getColor(R.color.toty_gold));
            tvTitle.setTextSize(14);
            tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
            content.addView(tvTitle);
        }

        String body = step.optString("body", "");
        TextView tvBody = new TextView(this);
        tvBody.setText(body);
        tvBody.setTextColor(getColor(R.color.auth_foreground));
        tvBody.setTextSize(14);
        content.addView(tvBody);

        String expr = step.optString("expr", "");
        if (!expr.isEmpty()) {
            TextView tvExpr = new TextView(this);
            tvExpr.setText(expr);
            tvExpr.setTextColor(getColor(R.color.toty_cyan));
            tvExpr.setTextSize(14);
            tvExpr.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.ITALIC);
            tvExpr.setPadding(12, 8, 12, 8);
            tvExpr.setBackgroundColor(getColor(R.color.black));
            LinearLayout.LayoutParams exprParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            exprParams.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
            tvExpr.setLayoutParams(exprParams);
            content.addView(tvExpr);
        }

        row.addView(stepNumber);
        row.addView(content);
        parent.addView(row);
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