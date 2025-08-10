package com.example.se07101campusexpenses.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.model.Budget;

import java.util.List;

public class BudgetRVAdapter extends RecyclerView.Adapter<BudgetRVAdapter.BudgetItemViewHolder> {
    private List<Budget> budgetModels;
    public Context context;
    public OnClickListener clickListener;
    public interface OnClickListener {
        void onClick(int position, Budget budget); // Pass Budget object on click
    }
    public void setOnClickListener(OnClickListener clickListener){
        this.clickListener = clickListener;
    }
    public BudgetRVAdapter(List<Budget> model, Context context){
        this.budgetModels = model;
        this.context = context;
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
        holder.tvNameBudget.setText(model.name); // Changed from model.category to model.name
        holder.tvBudgetMoney.setText(String.valueOf(model.amount));
        // holder.tvBudgetPeriod.setText(model.period); // Uncomment if you add tvBudgetPeriod to layout
        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null){
                clickListener.onClick(position, model); // Pass budget object
            }
        });
    }

    @Override
    public int getItemCount() {
        if (budgetModels != null) {
            return budgetModels.size();
        }
        return 0;
    }

    public class BudgetItemViewHolder extends RecyclerView.ViewHolder{
        TextView tvNameBudget, tvBudgetMoney; // Potentially TextView tvBudgetPeriod;
        View itemView;
        public BudgetItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tvNameBudget = itemView.findViewById(R.id.tvNameBudget);
            tvBudgetMoney = itemView.findViewById(R.id.tvMoneyBudget);
            // tvBudgetPeriod = itemView.findViewById(R.id.tvBudgetPeriod); // Initialize if added
            itemView.setOnClickListener(view -> {
                if (clickListener != null){
                    // Pass the specific budget item on click
                    clickListener.onClick(getAdapterPosition(), budgetModels.get(getAdapterPosition()));
                }
            });
        }
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgetModels = budgets;
        notifyDataSetChanged();
    }
}
