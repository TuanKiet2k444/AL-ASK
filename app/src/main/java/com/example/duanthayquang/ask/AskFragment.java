package com.example.duanthayquang.ask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.duanthayquang.BuildConfig;
import com.example.duanthayquang.MainActivity;
import com.example.duanthayquang.R;
import com.example.duanthayquang.answer.AnswerActivity;
import com.example.duanthayquang.auth.LoginActivity;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.Profile;
import com.example.duanthayquang.database.ProfileDao;
import com.example.duanthayquang.database.QuestionAnswer;
import com.example.duanthayquang.database.QuestionAnswerDao;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AskFragment extends Fragment {

    private static final String TAG = "AskFragment";
    private static final String[] SUBJECTS = {"math", "science", "code", "history", "language", "other"};
    private static final int XP_PER_QUESTION = 50;

    private EditText editQuestion;
    private MaterialButton btnSend;
    private ProgressBar progressLoading;
    private ExecutorService executor;

    private View containerImagePreview;
    private ImageView ivPreview;
    private String selectedImageBase64 = null;
    private String selectedMimeType = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processSelectedImage(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ask, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editQuestion = view.findViewById(R.id.edit_question);
        btnSend = view.findViewById(R.id.btn_send);
        progressLoading = view.findViewById(R.id.progress_loading);
        containerImagePreview = view.findViewById(R.id.container_image_preview);
        ivPreview = view.findViewById(R.id.iv_preview);
        executor = Executors.newSingleThreadExecutor();

        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
                if (bottomNav != null) bottomNav.setSelectedItemId(R.id.nav_home);
            }
        });

        btnSend.setOnClickListener(v -> submitQuestion());

        view.findViewById(R.id.btn_suggestion_1).setOnClickListener(v ->
                editQuestion.setText("Solve quadratic: x² - 5x + 6 = 0"));
        view.findViewById(R.id.btn_suggestion_2).setOnClickListener(v ->
                editQuestion.setText("How does photosynthesis work?"));
        view.findViewById(R.id.btn_suggestion_3).setOnClickListener(v ->
                editQuestion.setText("Difference between let and const in JS"));
        view.findViewById(R.id.btn_suggestion_4).setOnClickListener(v ->
                editQuestion.setText("Key causes of WWII?"));
        
        view.findViewById(R.id.btn_upload_image).setOnClickListener(v -> 
                imagePickerLauncher.launch("image/*"));

        view.findViewById(R.id.btn_remove_image).setOnClickListener(v -> removeSelectedImage());
    }

    private void processSelectedImage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // Resize if too large (Gemini has limits, and to save bandwidth/latency)
            Bitmap resized = resizeBitmap(bitmap, 1024);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            byte[] bytes = outputStream.toByteArray();
            
            selectedImageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
            selectedMimeType = "image/jpeg";
            
            ivPreview.setImageBitmap(resized);
            containerImagePreview.setVisibility(View.VISIBLE);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to process image", e);
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeSelectedImage() {
        selectedImageBase64 = null;
        selectedMimeType = null;
        ivPreview.setImageDrawable(null);
        containerImagePreview.setVisibility(View.GONE);
    }

    private Bitmap resizeBitmap(Bitmap source, int maxSize) {
        int width = source.getWidth();
        int height = source.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(source, width, height, true);
    }

    private void submitQuestion() {
        String question = editQuestion.getText().toString().trim();
        if (TextUtils.isEmpty(question) && selectedImageBase64 == null) {
            Toast.makeText(requireContext(), R.string.ask_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(BuildConfig.GEMINI_API_KEY) || "YOUR_REAL_GEMINI_API_KEY".equals(BuildConfig.GEMINI_API_KEY)) {
            Log.e(TAG, "submit blocked: api key missing or placeholder. Key value: [" + BuildConfig.GEMINI_API_KEY + "]");
            Toast.makeText(requireContext(), R.string.ask_no_api_key, Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        SharedPreferences prefs = requireContext().getSharedPreferences(LoginActivity.PREFS_AUTH, LoginActivity.MODE_PRIVATE);
        long userId = prefs.getLong(LoginActivity.KEY_USER_ID, -1);

        AppDatabase db = AppDatabase.getInstance(requireContext());
        ProfileDao profileDao = db.profileDao();
        QuestionAnswerDao qaDao = db.questionAnswerDao();
        Profile profile = profileDao.getByUserId(userId);

        String level = profile != null ? profile.level : "high";
        String style = profile != null ? profile.style : "stepwise";
        String prompt = buildPrompt(question, level, style);

        executor.execute(() -> {
            try {
                String raw = GeminiClient.ask(BuildConfig.GEMINI_API_KEY, prompt, selectedImageBase64, selectedMimeType);
                if (raw == null || raw.isEmpty()) {
                    showError(getString(R.string.ask_error));
                    return;
                }

                JSONObject json = new JSONObject(extractJson(raw));
                String subject = json.optString("subject", "other");
                if (!isValidSubject(subject)) subject = "other";

                JSONArray stepsArray = json.getJSONArray("steps");
                String finalAnswer = json.getString("finalAnswer");

                QuestionAnswer qa = new QuestionAnswer();
                qa.userId = userId;
                qa.subject = subject;
                qa.question = TextUtils.isEmpty(question) ? "Image Inquiry" : question;
                qa.steps = stepsArray.toString();
                qa.finalAnswer = finalAnswer;
                qa.starred = false;
                qa.createdAt = System.currentTimeMillis();

                long qaId = qaDao.insert(qa);

                if (profile != null) {
                    profile.xp += XP_PER_QUESTION;
                    profileDao.update(profile);
                }

                requireActivity().runOnUiThread(() -> {
                    setLoading(false);
                    removeSelectedImage();
                    editQuestion.setText("");
                    Intent intent = new Intent(requireContext(), AnswerActivity.class);
                    intent.putExtra(AnswerActivity.EXTRA_QA_ID, qaId);
                    startActivity(intent);
                });
            } catch (Exception e) {
                Log.e(TAG, "submit failed", e);
                String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                showError("AI Error: " + errorMsg);
            }
        });
    }

    private void showError(String message) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            setLoading(false);
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        });
    }

    private void setLoading(boolean loading) {
        btnSend.setEnabled(!loading);
        btnSend.setText(loading ? "Processing..." : "Send");
        progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String buildPrompt(String question, String level, String style) {
        String levelText = "junior".equals(level) ? "middle school student (grades 6-9)" :
                ("uni".equals(level) ? "university student" : "high school student (grades 10-12)");
        String styleText = "short".equals(style) ? "Answer very briefly - go straight to the answer." :
                ("detailed".equals(style) ? "Explain in detail with examples." : "Explain step by step, one idea per step.");

        String prompt = "You are AI Study Mentor, a study tutor in English for a " + levelText + ".\n" +
                styleText + "\n" +
                "Always answer in English. Put math formulas in the \"expr\" field of the steps.\n" +
                "Classify the subject correctly (math/science/code/history/language/other).\n" +
                "Return ONLY valid JSON (no markdown) with this structure:\n" +
                "{\n" +
                "  \"subject\": \"math|science|code|history|language|other\",\n" +
                "  \"steps\": [{\"title\": \"optional\", \"body\": \"required\", \"expr\": \"optional\"}],\n" +
                "  \"finalAnswer\": \"string\"\n" +
                "}\n\n";
        
        if (!TextUtils.isEmpty(question)) {
            prompt += "Question: " + question;
        } else {
            prompt += "Please analyze the provided image and provide a study-related explanation.";
        }
        
        return prompt;
    }

    private String extractJson(String raw) {
        String text = raw.trim();
        if (text.startsWith("```")) {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start >= 0 && end > start) return text.substring(start, end + 1);
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }

    private boolean isValidSubject(String subject) {
        for (String s : SUBJECTS) if (s.equals(subject)) return true;
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executor != null) executor.shutdown();
    }
}