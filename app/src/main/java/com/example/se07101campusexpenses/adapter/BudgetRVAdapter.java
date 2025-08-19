package com.example.se07101campusexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.model.Budget;

import java.text.NumberFormat; // Added import
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetRVAdapter extends RecyclerView.Adapter<BudgetRVAdapter.BudgetItemViewHolder> {
    private List<Budget> budgetModels;
    public Context context;
    public OnClickListener clickListener;
    private NumberFormat vndFormat; // Added for currency formatting

    public interface OnClickListener {
        void onClick(int position, Budget budget); // Pass Budget object on click
    }

    public void setOnClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public BudgetRVAdapter(List<Budget> model, Context context) {
        this.budgetModels = model != null ? new ArrayList<>(model) : new ArrayList<>();
        this.context = context;
        this.vndFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Initialize formatter
        this.vndFormat.setMaximumFractionDigits(0); // VND usually doesn't show decimals
    }

    @NonNull
    @Override
    public BudgetRVAdapter.BudgetItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_item_view, parent, false);
        return new BudgetItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetRVAdapter.BudgetItemViewHolder holder, int position) {
        Budget model = budgetModels.get(position);
        if (model != null) {
            holder.tvNameBudget.setText(model.getName());
            // Format budget amount using vndFormat
            holder.tvBudgetMoney.setText(vndFormat.format(model.getAmount()));
            holder.tvBudgetDescription.setText(model.getDescription());
            holder.tvBudgetPeriod.setText(model.getPeriod());
            holder.itemView.setOnClickListener(view -> {
                if (clickListener != null) {
                    clickListener.onClick(position, model); // Pass budget object
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return budgetModels != null ? budgetModels.size() : 0;
    }

    public class BudgetItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameBudget, tvBudgetMoney, tvBudgetDescription, tvBudgetPeriod;
        View itemView;

        public BudgetItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvNameBudget = itemView.findViewById(R.id.tvNameBudget);
            tvBudgetMoney = itemView.findViewById(R.id.tvMoneyBudget);
            tvBudgetDescription = itemView.findViewById(R.id.tvBudgetDescription);
            tvBudgetPeriod = itemView.findViewById(R.id.tvBudgetPeriod);
        }
    }

    public void setBudgets(List<Budget> newBudgets) {
        List<Budget> oldList = this.budgetModels != null ? this.budgetModels : new ArrayList<>();
        List<Budget> newList = newBudgets != null ? new ArrayList<>(newBudgets) : new ArrayList<>();
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return oldList.size(); }
            @Override
            public int getNewListSize() { return newList.size(); }
            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Budget o = oldList.get(oldItemPosition);
                Budget n = newList.get(newItemPosition);
                return o.getId() == n.getId();
            }
            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Budget o = oldList.get(oldItemPosition);
                Budget n = newList.get(newItemPosition);
                return o.getAmount() == n.getAmount()
                        && equalsStr(o.getName(), n.getName())
                        && equalsStr(o.getPeriod(), n.getPeriod())
                        && equalsStr(o.getDescription(), n.getDescription());
            }
            private boolean equalsStr(String a, String b) { return a == null ? b == null : a.equals(b); }
        });
        this.budgetModels = newList;
        diff.dispatchUpdatesTo(this);
    }
}
