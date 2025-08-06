package com.example.se07101campusexpenses.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.model.RecurringExpense;
import java.util.List;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.ViewHolder> {

    private final List<RecurringExpense> recurringExpenseList;

    public RecurringExpenseAdapter(List<RecurringExpense> recurringExpenseList) {
        this.recurringExpenseList = recurringExpenseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecurringExpense expense = recurringExpenseList.get(position);
        holder.tvDescription.setText(expense.description);
        holder.tvAmount.setText(String.valueOf(expense.amount));
        holder.tvCategory.setText(expense.category);
        holder.tvDate.setText(holder.itemView.getContext().getString(R.string.date_range, expense.startDate, expense.endDate));
    }

    @Override
    public int getItemCount() {
        return recurringExpenseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvAmount, tvCategory, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvExpenseDescription);
            tvAmount = itemView.findViewById(R.id.tvExpenseAmount);
            tvCategory = itemView.findViewById(R.id.tvExpenseCategory);
            tvDate = itemView.findViewById(R.id.tvExpenseDate);
        }
    }
}
