package com.amol.interview_practical.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amol.interview_practical.Model.api.Datum;
import com.amol.interview_practical.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Datum> list = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    public int VIEW_TYPE = 1;
    public int VIEW_TYPE_LOad = 0;

    public HomeAdapter() {
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mInflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == VIEW_TYPE) {
            view = mInflater.inflate(R.layout.lay_item_data, parent, false);
            return new ViewHolders(view);
        } else {
            view = mInflater.inflate(R.layout.lay_pg, parent, false);
            return new LoadingHoldeer(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position) == null ? VIEW_TYPE_LOad : VIEW_TYPE;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolders) {
            ViewHolders holder1 = ((ViewHolders) holder);
            Datum animal = list.get(position);
            Glide.with(holder1.imageView.getContext()).load(animal.getAvatar()).placeholder(R.drawable.place_holder).into(holder1.imageView);

            holder1.tvName.setText(animal.getFirstName() + " " + animal.getLastName());
            holder1.tvEmail.setText(animal.getEmail());

        } else {

        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return list.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvName, tvEmail;
        ImageView imageView;

        ViewHolders(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            imageView = itemView.findViewById(R.id.imgItem);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public class LoadingHoldeer extends RecyclerView.ViewHolder {
        ProgressBar imageView;

        LoadingHoldeer(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.lay__pg);
        }

    }


    // convenience method for getting data at click position

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    synchronized public void addItem(List<Datum> imageModel) {
        if (list == null)
            list = new ArrayList<>();
        list.addAll(imageModel);
        if (list.size() > 0)
            notifyItemInserted(list.size() - 1);
    }

    public void notifyListChange(ArrayList<Datum> gridList) {
        this.list = gridList;
        notifyDataSetChanged();
    }

}