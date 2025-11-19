package com.pongploydev.priyankvex.paintapp.module;

import android.content.Context;
import android.content.res.Resources;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.pongploydev.priyankvex.paintapp.adapter.MenuAdapter;
import com.pongploydev.priyankvex.paintapp.holder.MenuItemViewHolder;
import com.pongploydev.priyankvex.paintapp.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MenuModule {

    private Context context;
    private RecyclerView recyclerView;
    private MenuAdapter adapter;
    private List<MenuItem> menuItems;

    public MenuModule(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(context, 4);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void setMenuItems(List<MenuItem> items) {
        this.menuItems = items;
        if (adapter != null) {
            adapter.updateData(items);
        }
    }

    public void setOnItemClickListener(MenuItemViewHolder.OnItemClickListener itemListener,
                                       MenuItemViewHolder.OnSoundClickListener soundListener) {
        adapter = new MenuAdapter(menuItems, itemListener, soundListener);
        recyclerView.setAdapter(adapter);
    }

    public void updateGridColumns(int columns) {
        GridLayoutManager layoutManager = new GridLayoutManager(context, columns);
        recyclerView.setLayoutManager(layoutManager);
    }

    // Helper method to create default menu items
    public static List<MenuItem> createDefaultMenuItems(Context context) {
        List<MenuItem> items = new ArrayList<>();
        Resources res = context.getResources();
        String packageName = context.getPackageName();

        for (int i = 1; i <= 44; i++) {
            try {
                String stringName = "k_" + i;
                String drawableName = "k_" + getNumberName(i);

                int stringId = res.getIdentifier(stringName, "string", packageName);
                int drawableId = res.getIdentifier(drawableName, "drawable", packageName);

                if (stringId != 0 && drawableId != 0) {
                    String itemName = res.getString(stringId);
                    items.add(new MenuItem(itemName, drawableId, i - 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return items;
    }

    private static String getNumberName(int number) {
        if (number <= 20) {
            return getSimpleNumberName(number);
        } else if (number <= 29) {
            return "twenty_" + getSimpleNumberName(number - 20);
        } else if (number <= 39) {
            return "thirty_" + getSimpleNumberName(number - 30);
        } else {
            return "forty_" + getSimpleNumberName(number - 40);
        }
    }

    private static String getSimpleNumberName(int number) {
        switch (number) {
            case 1: return "one";
            case 2: return "two";
            case 3: return "three";
            case 4: return "four";
            case 5: return "five";
            case 6: return "six";
            case 7: return "seven";
            case 8: return "eight";
            case 9: return "nine";
            case 10: return "ten";
            case 11: return "eleven";
            case 12: return "twelve";
            case 13: return "thirteen";
            case 14: return "fourteen";
            case 15: return "fifteen";
            case 16: return "sixteen";
            case 17: return "seventeen";
            case 18: return "eighteen";
            case 19: return "nineteen";
            case 20: return "twenty";
            default: return "";
        }
    }

    public int getItemCount() {
        return menuItems != null ? menuItems.size() : 0;
    }

    public MenuItem getItemAt(int index) {
        if (menuItems != null && index >= 0 && index < menuItems.size()) {
            return menuItems.get(index);
        }
        return null;
    }

}