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

import java.util.ArrayList;

public class BudgetRVAdapter extends RecyclerView.Adapter<BudgetRVAdapter.BudgetItemViewHolder> {
    public ArrayList<BudgetModel> budgetModels;
    public Context context;
    public OnClickListener clickListener;
    public interface OnClickListener {
        void onClick(int position);
    }
    public void setOnClickListener(OnClickListener clickListener){
        this.clickListener = clickListener;
    }
    public BudgetRVAdapter(ArrayList<BudgetModel> model, Context context){
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
        BudgetModel model = budgetModels.get(position);
        holder.tvNameBudget.setText(model.getBudgetName());
        holder.tvBudgetMoney.setText(String.valueOf(model.getBudgetMoney()));
        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null){
                clickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetModels.size(); // so luong pham list view
    }

    public class BudgetItemViewHolder extends RecyclerView.ViewHolder{
        TextView tvNameBudget, tvBudgetMoney;
        View itemView;
        public BudgetItemViewHolder(@NonNull View itemView) {
            super(itemView.getRootView());
            this.itemView = itemView;
            tvNameBudget = itemView.findViewById(R.id.tvNameBudget);
            tvBudgetMoney = itemView.findViewById(R.id.tvMoneyBudget);
            itemView.setOnClickListener(view -> {
                if (clickListener != null){
                    clickListener.onClick(getAdapterPosition());
                }
            });
        }
    }
}
