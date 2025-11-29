package com.example.biscuit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> implements Filterable {
    private List<ResolveInfo> apps;
    private final List<ResolveInfo> appsFull;
    private final PackageManager pm;
    private final Context ctx;

    public AppListAdapter(Context ctx, List<ResolveInfo> apps, PackageManager pm) {
        this.ctx = ctx;
        this.apps = new ArrayList<>(apps);
        this.appsFull = new ArrayList<>(apps);
        this.pm = pm;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.app_items, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ResolveInfo info = apps.get(position);
        try {
            holder.name.setText(info.loadLabel(pm).toString());
            holder.icon.setImageDrawable(info.loadIcon(pm));
        } catch (Exception e) {
            holder.name.setText(info.activityInfo.packageName);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, BlockSettingsActivity.class);
            intent.putExtra("package", info.activityInfo.packageName);
            intent.putExtra("name", info.loadLabel(pm).toString());
            ctx.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return apps.size(); }

    @Override
    public Filter getFilter() {
        return appFilter;
    }

    private final Filter appFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ResolveInfo> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(appsFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ResolveInfo item : appsFull) {
                    try {
                        String appName = item.loadLabel(pm).toString();
                        if (appName.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            apps.clear();
            if (results.values != null) {
                apps.addAll((List<ResolveInfo>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name;
        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
        }
    }
}
