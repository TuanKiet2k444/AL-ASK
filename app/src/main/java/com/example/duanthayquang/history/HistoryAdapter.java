package com.example.duanthayquang.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.duanthayquang.R;
import com.example.duanthayquang.database.QuestionAnswer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<QuestionAnswer> items;
    private final OnItemClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(QuestionAnswer item);
    }

    public HistoryAdapter(List<QuestionAnswer> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestionAnswer item = items.get(position);
        holder.tvQuestion.setText(item.question);
        holder.tvSubject.setText(item.subject.toUpperCase());
        holder.tvDate.setText(dateFormat.format(new Date(item.createdAt)));
        holder.ivStar.setVisibility(item.starred ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvSubject, tvDate;
        View ivStar;

        ViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivStar = itemView.findViewById(R.id.iv_star);
        }
    }
}