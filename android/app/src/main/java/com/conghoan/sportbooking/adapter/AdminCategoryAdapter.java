package com.conghoan.sportbooking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conghoan.sportbooking.R;

import java.util.List;
import java.util.Map;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {

    public interface OnCategoryActionListener {
        void onEditClick(Map<String, Object> category, int position);
        void onDeleteClick(Map<String, Object> category, int position);
    }

    private Context context;
    private List<Map<String, Object>> categoryList;
    private OnCategoryActionListener actionListener;

    public AdminCategoryAdapter(Context context, List<Map<String, Object>> categoryList, OnCategoryActionListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> category = categoryList.get(position);

        String name = getStr(category, "name");
        String iconUrl = getStr(category, "iconUrl");

        holder.tvName.setText(name.isEmpty() ? "N/A" : name);
        holder.tvIconUrl.setText(iconUrl.isEmpty() ? "--" : iconUrl);

        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditClick(category, position);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClick(category, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvIconUrl;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_category_name);
            tvIconUrl = itemView.findViewById(R.id.tv_category_icon_url);
            btnEdit = itemView.findViewById(R.id.btn_edit_category);
            btnDelete = itemView.findViewById(R.id.btn_delete_category);
        }
    }
}
