package com.example.duanthayquang.history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duanthayquang.R;
import com.example.duanthayquang.answer.AnswerActivity;
import com.example.duanthayquang.auth.LoginActivity;
import com.example.duanthayquang.database.AppDatabase;
import com.example.duanthayquang.database.QuestionAnswer;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private HistoryAdapter adapter;
    private final List<QuestionAnswer> allItems = new ArrayList<>();
    private final List<QuestionAnswer> filteredItems = new ArrayList<>();

    private MaterialButton filterAll, filterMath, filterScience, filterCode, filterHistory;
    private String currentSubjectFilter = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_history);
        emptyState = view.findViewById(R.id.empty_state);
        EditText editSearch = view.findViewById(R.id.edit_search);

        filterAll = view.findViewById(R.id.filter_all);
        filterMath = view.findViewById(R.id.filter_math);
        filterScience = view.findViewById(R.id.filter_science);
        filterCode = view.findViewById(R.id.filter_code);
        filterHistory = view.findViewById(R.id.filter_history);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoryAdapter(filteredItems, item -> {
            Intent intent = new Intent(requireContext(), AnswerActivity.class);
            intent.putExtra(AnswerActivity.EXTRA_QA_ID, item.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString(), currentSubjectFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        filterAll.setOnClickListener(v -> applySubjectFilter(""));
        filterMath.setOnClickListener(v -> applySubjectFilter("math"));
        filterScience.setOnClickListener(v -> applySubjectFilter("science"));
        filterCode.setOnClickListener(v -> applySubjectFilter("code"));
        filterHistory.setOnClickListener(v -> applySubjectFilter("history"));

        if (getArguments() != null) {
            currentSubjectFilter = getArguments().getString("filter_subject", "");
        }

        loadHistory();
    }

    private void applySubjectFilter(String subject) {
        currentSubjectFilter = subject;
        updateFilterUI();
        filter("", currentSubjectFilter);
    }

    private void updateFilterUI() {
        resetFilters();
        if (currentSubjectFilter.isEmpty()) {
            setFilterActive(filterAll);
        } else {
            switch (currentSubjectFilter) {
                case "math": setFilterActive(filterMath); break;
                case "science": setFilterActive(filterScience); break;
                case "code": setFilterActive(filterCode); break;
                case "history": setFilterActive(filterHistory); break;
            }
        }
    }

    private void resetFilters() {
        setFilterInactive(filterAll);
        setFilterInactive(filterMath);
        setFilterInactive(filterScience);
        setFilterInactive(filterCode);
        setFilterInactive(filterHistory);
    }

    private void setFilterActive(MaterialButton btn) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.toty_gold)));
        btn.setTextColor(getResources().getColor(R.color.black));
        btn.setStrokeWidth(0);
    }

    private void setFilterInactive(MaterialButton btn) {
        btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT));
        btn.setTextColor(getResources().getColor(R.color.auth_foreground));
        btn.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
        btn.setStrokeColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.auth_input_ring)));
    }

    private void loadHistory() {
        if (getContext() == null) return;

        long userId = requireContext()
                .getSharedPreferences(LoginActivity.PREFS_AUTH, Context.MODE_PRIVATE)
                .getLong(LoginActivity.KEY_USER_ID, -1);
        if (userId == -1) return;

        List<QuestionAnswer> history = AppDatabase.getInstance(requireContext())
                .questionAnswerDao()
                .getAllByUserId(userId);

        allItems.clear();
        allItems.addAll(history);
        updateFilterUI();
        filter("", currentSubjectFilter);
    }

    private void filter(String query, String subject) {
        filteredItems.clear();
        String lowerQuery = query.toLowerCase();
        
        for (QuestionAnswer item : allItems) {
            boolean matchesQuery = query.isEmpty() || item.question.toLowerCase().contains(lowerQuery);
            boolean matchesSubject = subject.isEmpty() || item.subject.equalsIgnoreCase(subject);
            
            if (matchesQuery && matchesSubject) {
                filteredItems.add(item);
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}