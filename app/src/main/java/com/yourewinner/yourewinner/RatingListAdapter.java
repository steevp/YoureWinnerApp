package com.yourewinner.yourewinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RatingListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private final int[] ratingIDs = {23, 22, 21, 25, 26, 20, 19, 24, 101, 18, 17, 16, 102, 104, 103, 111, 116, 117, 118, 119, 120};

    public RatingListAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
    }

    @Override
    public int getCount() {
        return ratingIDs.length;
    }

    @Override
    public Object getItem(int position) {
        return ratingIDs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.rating_list_item, null);
            holder = new ViewHolder();
            holder.ratingImage = (ImageView) convertView.findViewById(R.id.rating_image);
            holder.ratingText = (TextView) convertView.findViewById(R.id.rating_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        int ratingID = (int) getItem(position);
        switch (ratingID) {
            case 23:
                holder.ratingImage.setImageResource(R.mipmap.rating_trophy);
                holder.ratingText.setText("Trophy");
                break;
            case 22:
                holder.ratingImage.setImageResource(R.mipmap.rating_thumbup);
                holder.ratingText.setText("Thumb Up");
                break;
            case 21:
                holder.ratingImage.setImageResource(R.mipmap.rating_thumbdown);
                holder.ratingText.setText("Thumb Down");
                break;
            case 25:
                holder.ratingImage.setImageResource(R.mipmap.rating_turd);
                holder.ratingText.setText("Turd");
                break;
            case 26:
                holder.ratingImage.setImageResource(R.mipmap.rating_gay);
                holder.ratingText.setText("Gay");
                break;
            case 20:
                holder.ratingImage.setImageResource(R.mipmap.rating_heart);
                holder.ratingText.setText("Heart");
                break;
            case 19:
                holder.ratingImage.setImageResource(R.mipmap.rating_smug);
                holder.ratingText.setText("Smug");
                break;
            case 24:
                holder.ratingImage.setImageResource(R.mipmap.rating_cheese);
                holder.ratingText.setText("Cheesy");
                break;
            case 101:
                holder.ratingImage.setImageResource(R.mipmap.rating_aggro);
                holder.ratingText.setText("Aggro");
                break;
            case 18:
                holder.ratingImage.setImageResource(R.mipmap.rating_twisted);
                holder.ratingText.setText("Twisted");
                break;
            case 17:
                holder.ratingImage.setImageResource(R.mipmap.rating_zigsd);
                holder.ratingText.setText("ZIGS'd");
                break;
            case 16:
                holder.ratingImage.setImageResource(R.mipmap.rating_old);
                holder.ratingText.setText("Old");
                break;
            case 102:
                holder.ratingImage.setImageResource(R.mipmap.rating_baby);
                holder.ratingText.setText("Baby");
                break;
            case 104:
                holder.ratingImage.setImageResource(R.mipmap.rating_weed);
                holder.ratingText.setText("Weed");
                break;
            case 103:
                holder.ratingImage.setImageResource(R.mipmap.rating_meta);
                holder.ratingText.setText("Meta");
                break;
            case 111:
                holder.ratingImage.setImageResource(R.mipmap.rating_ballin);
                holder.ratingText.setText("BALLIN'");
                break;
            case 116:
                holder.ratingImage.setImageResource(R.mipmap.rating_coorslight);
                holder.ratingText.setText("Coors Light");
                break;
            case 117:
                holder.ratingImage.setImageResource(R.mipmap.rating_america);
                holder.ratingText.setText("AMERICA");
                break;
            case 118:
                holder.ratingImage.setImageResource(R.mipmap.rating_hipster);
                holder.ratingText.setText("Hipster");
                break;
            case 119:
                holder.ratingImage.setImageResource(R.mipmap.rating_scroogled);
                holder.ratingText.setText("#scroogled");
                break;
            case 120:
                holder.ratingImage.setImageResource(R.mipmap.rating_mistletoe);
                holder.ratingText.setText("Mistletoe");
                break;
            default:
                holder.ratingImage.setImageResource(R.mipmap.ic_launcher);
                holder.ratingText.setText("Unknown");
                break;
        }

        return convertView;
    }

    private static class ViewHolder {
        public ImageView ratingImage;
        public TextView ratingText;
    }
}
