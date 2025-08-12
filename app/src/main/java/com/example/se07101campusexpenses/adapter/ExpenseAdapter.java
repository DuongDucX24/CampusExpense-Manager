package com.example.se07101campusexpenses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.model.Expense;

import java.text.NumberFormat; // Added import
import java.util.List;
import java.util.Locale; // Added import

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnItemClickListener listener;
    private NumberFormat vndFormat; // Added for currency formatting

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
        this.vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Initialize formatter
        this.vndFormat.setMaximumFractionDigits(0); // VND usually doesn't show decimals
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
        // Format expense amount using vndFormat
        holder.tvExpenseAmount.setText(vndFormat.format(currentExpense.amount));
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
