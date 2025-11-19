package com.pongploydev.priyankvex.paintapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;


import com.pongploydev.priyankvex.paintapp.holder.MenuItemViewHolder;
import com.pongploydev.priyankvex.paintapp.model.MenuItem;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.List;
public class MenuAdapter extends RecyclerView.Adapter<MenuItemViewHolder> {

    private List<MenuItem> itemList;
    private MenuItemViewHolder.OnItemClickListener itemListener;
    private MenuItemViewHolder.OnSoundClickListener soundListener;

    public MenuAdapter(List<MenuItem> itemList,
                       MenuItemViewHolder.OnItemClickListener itemListener,
                       MenuItemViewHolder.OnSoundClickListener soundListener) {
        this.itemList = itemList;
        this.itemListener = itemListener;
        this.soundListener = soundListener;
    }

    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_list, parent, false);
        return new MenuItemViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(MenuItemViewHolder holder, int position) {
        holder.bind(itemList.get(position), itemListener, soundListener);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateData(List<MenuItem> newItems) {
        this.itemList = newItems;
        notifyDataSetChanged();
    }
}