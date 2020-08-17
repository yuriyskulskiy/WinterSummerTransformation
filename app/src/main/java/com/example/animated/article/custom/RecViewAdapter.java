package com.example.animated.article.custom;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.animated.R;

import java.util.List;

public class RecViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int INSERT_POSITION = 10;

    private final static int REGULAR_VIEW_TYPE = 1;
    private final static int MAGIC_VIEW_TYPE = 2;

    private List<RegularData> dataSet;

    public RecViewAdapter(List<RegularData> dataSet) {
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rootView;
        if (viewType == REGULAR_VIEW_TYPE) {
            rootView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.regular_recycler_view_item, parent, false);
            return new RegularViewHolder(rootView);
        }

        if (viewType == MAGIC_VIEW_TYPE) {
            rootView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.transform_item, parent, false);
            return new AnimationViewHolder(rootView);
        }

        throw new UnsupportedOperationException("Unsupported viewType: " + viewType);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == REGULAR_VIEW_TYPE) {
            RegularViewHolder viewHolder = (RegularViewHolder) holder;
            RegularData currentDataItem;
            if (position > INSERT_POSITION) {
                currentDataItem = dataSet.get(--position);
            } else {
                currentDataItem = dataSet.get(position);
            }
            viewHolder.textView.setText(currentDataItem.getText());
        } else {
            AnimationViewHolder viewHolder = (AnimationViewHolder) holder;
            viewHolder.animatedLayout.invalidateText();
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size() + 1;
    }

    private boolean isMagicItem(int position) {
        return position == INSERT_POSITION;
    }

    @Override
    public int getItemViewType(int position) {
        if (isMagicItem(position)) {
            return MAGIC_VIEW_TYPE;
        } else {
            return REGULAR_VIEW_TYPE;
        }
    }

    public class RegularViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;

        public RegularViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.data_text);
        }
    }

    public class AnimationViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public AnimatedLayout animatedLayout;

        public AnimationViewHolder(View itemView) {
            super(itemView);
            animatedLayout = itemView.findViewById(R.id.transformItem);
        }
    }
}
