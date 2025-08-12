package com.example.se07101campusexpenses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.model.Expense;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder> {

    private OnItemClickListener listener;
    private final NumberFormat vndFormat;

    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ExpenseAdapter() {
        super(DIFF_CALLBACK);
        this.vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.vndFormat.setMaximumFractionDigits(0);
    }

    // Private method to update expense list for compatibility with existing code
    // This method is not directly used in this class but may be called from outside
    public void submitExpenseList(List<Expense> expenses) {
        submitList(expenses);
    }

    // DiffUtil implementation for efficient updates
    private static final DiffUtil.ItemCallback<Expense> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Expense oldItem, @NonNull Expense newItem) {
            return oldItem.getDescription().equals(newItem.getDescription()) &&
                   oldItem.getCategory().equals(newItem.getCategory()) &&
                   oldItem.getAmount() == newItem.getAmount() &&
                   oldItem.getDate().equals(newItem.getDate());
        }
    };

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense currentExpense = getItem(position);
        holder.tvExpenseDescription.setText(currentExpense.getDescription());
        holder.tvExpenseCategory.setText(currentExpense.getCategory());
        holder.tvExpenseAmount.setText(vndFormat.format(currentExpense.getAmount()));
        holder.tvExpenseDate.setText(currentExpense.getDate());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentExpense);
            }
        });

        // Set stable IDs for animation stability
        holder.itemView.setTag(currentExpense.getId());
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvExpenseDescription;
        private final TextView tvExpenseCategory;
        private final TextView tvExpenseAmount;
        private final TextView tvExpenseDate;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvExpenseCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvExpenseAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvExpenseDate = itemView.findViewById(R.id.tvExpenseDate);
        }
    }
}
