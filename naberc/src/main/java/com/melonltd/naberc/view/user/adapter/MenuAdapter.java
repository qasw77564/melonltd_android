package com.melonltd.naberc.view.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.melonltd.naberc.R;

import java.util.List;

public class MenuAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<String> list;

    public MenuAdapter(Context context, List list) {
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MenuItem item = null;
        if (view == null) {
            view = inflater.inflate(R.layout.user_menu_item, null);
            item = MenuItem.valueOf(view);
            view.setTag(item);
        } else {
            item = (MenuItem) view.getTag();
        }
        item.itemPriceText.setText("3" + i);
        return view;
    }

    static class MenuItem {
        private ImageView itemIconImageView;
        private TextView itemNameText, itemPriceText;

        public static MenuItem valueOf(View v) {
            MenuItem item = new MenuItem();
            item.itemIconImageView = v.findViewById(R.id.itemIconImageView);
            item.itemNameText = v.findViewById(R.id.itemNameText);
            item.itemPriceText = v.findViewById(R.id.itemPriceText);
            return item;
        }
    }
}