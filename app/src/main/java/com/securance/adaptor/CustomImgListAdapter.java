package com.securance.adaptor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.securance.R;

import java.util.HashMap;
import java.util.List;

public class CustomImgListAdapter extends BaseAdapter implements View.OnClickListener {
    private Context mcontext;
    private LayoutInflater inflater;
    private List<HashMap<String, Object>> rowItems;
    // private View promptsView;

    public CustomImgListAdapter(Context mcontext, List<HashMap<String, Object>> rowItems) {
        this.mcontext = mcontext;
        this.rowItems = rowItems;
    }

    public List getRowItems() {
        return this.rowItems;
    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int location) {
        return rowItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.listimgitem, null);


        // getting data for the row
        String details = rowItems.get(position).get("Phone").toString();
        if( details != null && details .length() > 0) {
            ((TextView) convertView.findViewById(R.id.details_item)).setText(details);
        } else  {
            ((TextView) convertView.findViewById(R.id.details_item)).setVisibility(View.GONE);
        }

        String title = rowItems.get(position).get("Name").toString();
        if( title != null && title .length() > 0) {
            ((TextView) convertView.findViewById(R.id.title_item)).setText(title);
        } else  {
            ((TextView) convertView.findViewById(R.id.title_item)).setVisibility(View.GONE);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        String iconURL = rowItems.get(position).get("icon").toString();
        if( iconURL != null && iconURL .length() > 10) {
            Glide.with(mcontext).load(iconURL)
                    .thumbnail(0.5f)
                    .into(icon);
        }

        return convertView;
    }

    @Override
    public void onClick(View v) {

    }
}
