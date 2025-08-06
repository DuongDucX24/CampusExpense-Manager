package com.example.se07101campusexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.database.BudgetModel;

import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<BudgetModel> budgetList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(BudgetModel budget);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public BudgetAdapter(List<BudgetModel> budgetList) {
        this.budgetList = budgetList;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.budget_item, parent, false);
        return new BudgetViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetModel currentBudget = budgetList.get(position);
        holder.tvBudgetName.setText(currentBudget.getName());
        holder.tvBudgetAmount.setText(String.format("Amount: $%.2f", currentBudget.getAmount()));
        holder.tvBudgetPeriod.setText(currentBudget.getPeriod());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentBudget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        private TextView tvBudgetName;
        private TextView tvBudgetAmount;
        private TextView tvBudgetPeriod;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetName = itemView.findViewById(R.id.tvBudgetName);
            tvBudgetAmount = itemView.findViewById(R.id.tvBudgetAmount);
            tvBudgetPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
        }
    }
}
