package com.yourewinner.yourewinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by steven on 1/8/16.
 */
public class ProfileViewAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    //private ArrayList<Object> mProfileItems;
    private Map<String,Object> mProfileData;

    public ProfileViewAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
    }

    public void updateData(Map<String,Object> data) {
        mProfileData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_profile_view, null);
            holder = new ViewHolder();
            holder.profileItemLabel = (TextView) convertView.findViewById(R.id.profile_item_label);
            holder.profileItemValue = (TextView) convertView.findViewById(R.id.profile_item_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    private static class ViewHolder {
        public TextView profileItemLabel;
        public TextView profileItemValue;
    }
}
