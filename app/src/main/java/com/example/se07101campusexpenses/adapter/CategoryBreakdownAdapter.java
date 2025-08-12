package com.example.se07101campusexpenses.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.se07101campusexpenses.R;
import com.example.se07101campusexpenses.model.CategorySum;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CategoryBreakdownAdapter extends RecyclerView.Adapter<CategoryBreakdownAdapter.CategoryViewHolder> {

    private final List<CategorySum> categorySums;
    private final NumberFormat currencyFormat;

    public CategoryBreakdownAdapter(List<CategorySum> categorySums) {
        this.categorySums = categorySums;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.currencyFormat.setMaximumFractionDigits(0);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_breakdown, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategorySum categorySum = categorySums.get(position);
        holder.tvCategoryName.setText(categorySum.getCategory());
        holder.tvCategoryAmount.setText(currencyFormat.format(categorySum.getSum()));

        // Assign a color indicator based on position
        holder.viewColorIndicator.setBackgroundColor(getCategoryColor(position));
    }

    @Override
    public int getItemCount() {
        return categorySums.size();
    }

    private int getCategoryColor(int position) {
        // Simple way to generate different colors for each category
        // This matches with the color palette used in the pie chart
        int[] colors = {
                Color.rgb(255, 123, 124),  // Red
                Color.rgb(98, 182, 239),   // Blue
                Color.rgb(255, 187, 68),   // Yellow/Orange
                Color.rgb(113, 192, 143),  // Green
                Color.rgb(170, 128, 255),  // Purple
                Color.rgb(255, 157, 216)   // Pink
        };

        return colors[position % colors.length];
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        View viewColorIndicator;
        TextView tvCategoryName;
        TextView tvCategoryAmount;

        CategoryViewHolder(View itemView) {
            super(itemView);
            viewColorIndicator = itemView.findViewById(R.id.viewColorIndicator);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryAmount = itemView.findViewById(R.id.tvCategoryAmount);
        }
    }
}
