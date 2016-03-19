package com.yourewinner.yourewinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by steven on 3/18/16.
 */
public class RatingViewAdapter extends BaseAdapter {
    private final static Map<String,Integer> RATINGS = new HashMap<String,Integer>();

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Object> mRatings;

    public RatingViewAdapter(Context context, LayoutInflater inflater, Object[] ratings) {
        mContext = context;
        mInflater = inflater;
        mRatings = new ArrayList<Object>(Arrays.asList(ratings));
        initializeRatings();
    }

    @Override
    public int getCount() {
        return mRatings.size();
    }

    @Override
    public Object getItem(int position) {
        return mRatings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rating_view_item, null);
            holder = new ViewHolder();
            holder.username = (TextView) convertView.findViewById(R.id.username);
            holder.rating1 = (ImageView) convertView.findViewById(R.id.rating1);
            holder.rating2 = (ImageView) convertView.findViewById(R.id.rating2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Map<String,Object> item = (Map<String,Object>) getItem(position);
        final String username = (String) item.get("username");
        final Object[] ratings = (Object[]) item.get("ratings");

        holder.username.setText(username);
        if (ratings.length == 2) {
            final String rating1 = (String) ratings[0];
            final String rating2 = (String) ratings[1];
            holder.rating1.setImageResource(RATINGS.get(rating1));
            holder.rating2.setImageResource(RATINGS.get(rating2));
            holder.rating2.setVisibility(View.VISIBLE);
        } else if (ratings.length == 1) {
            final String rating1 = (String) ratings[0];
            holder.rating1.setImageResource(RATINGS.get(rating1));
            holder.rating2.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void initializeRatings() {
        RATINGS.put("23", R.mipmap.rating_trophy);
        RATINGS.put("22", R.mipmap.rating_thumbup);
        RATINGS.put("21", R.mipmap.rating_thumbdown);
        RATINGS.put("25", R.mipmap.rating_turd);
        RATINGS.put("26", R.mipmap.rating_gay);
        RATINGS.put("20", R.mipmap.rating_heart);
        RATINGS.put("19", R.mipmap.rating_smug);
        RATINGS.put("24", R.mipmap.rating_cheese);
        RATINGS.put("101", R.mipmap.rating_aggro);
        RATINGS.put("18", R.mipmap.rating_twisted);
        RATINGS.put("17", R.mipmap.rating_zigsd);
        RATINGS.put("16", R.mipmap.rating_old);
        RATINGS.put("102", R.mipmap.rating_baby);
        RATINGS.put("104", R.mipmap.rating_weed);
        RATINGS.put("103", R.mipmap.rating_meta);
        RATINGS.put("111", R.mipmap.rating_ballin);
        RATINGS.put("116", R.mipmap.rating_coorslight);
        RATINGS.put("117", R.mipmap.rating_america);
        RATINGS.put("118", R.mipmap.rating_hipster);
        RATINGS.put("119", R.mipmap.rating_scroogled);
        RATINGS.put("120", R.mipmap.rating_mistletoe);
    }

    private static class ViewHolder {
        public TextView username;
        public ImageView rating1;
        public ImageView rating2;
    }

}
