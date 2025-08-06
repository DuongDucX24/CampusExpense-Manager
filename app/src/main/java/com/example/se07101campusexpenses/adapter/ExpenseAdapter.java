package com.example.se07101campusexpenses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.model.Expense;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense currentExpense = expenseList.get(position);
        holder.tvExpenseDescription.setText(currentExpense.description);
        holder.tvExpenseCategory.setText(currentExpense.category);
        holder.tvExpenseAmount.setText(String.format("Amount: $%.2f", currentExpense.amount));
        holder.tvExpenseDate.setText(currentExpense.date);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentExpense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExpenseDescription;
        private TextView tvExpenseCategory;
        private TextView tvExpenseAmount;
        private TextView tvExpenseDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvExpenseCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
        }
    }
}
