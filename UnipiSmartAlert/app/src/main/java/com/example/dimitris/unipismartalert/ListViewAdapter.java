package com.example.dimitris.unipismartalert;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

//Κλαση custom adapter για ListView
public class ListViewAdapter extends BaseAdapter {

    // Declare Variables

    Context mContext;
    LayoutInflater inflater;
    private ArrayList<DEvents> arraylist;

    public ListViewAdapter(Context context, ArrayList<DEvents> arraylist) {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = arraylist;

    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return arraylist.size();
    }

    @Override
    public DEvents getItem(int position) {
        return arraylist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_item,parent,false);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(arraylist.get(position).getString());
        return view;
    }


}
