package com.yourewinner.yourewinner;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by steven on 3/25/17.
 */

public class EmoteGridAdapter extends BaseAdapter {
    private Context mContext;

    private int[] mEmoteIds = {
            R.drawable.thumbsup, R.drawable.pacman,
            R.drawable.ateam, R.drawable.brbox,
            R.drawable.shaq, R.drawable.trophy,
            R.drawable.bump, R.drawable.dare,
            R.drawable.texan, R.drawable.lolwut,
            R.drawable.mj, R.drawable.ngage,
            R.drawable.mc, R.drawable.rocky,
            R.drawable.sslogo, R.drawable.winner,
            R.drawable.ballin, R.drawable.pika,
            R.drawable.barneyclap, R.drawable.barneykiss,
            R.drawable.facepalm, R.drawable.unhappy,
            R.drawable.volcanicity, R.drawable.kawaii,
            R.drawable.russian, R.drawable.headbang,
            R.drawable.running, R.drawable.mrtwinner,
            R.drawable.timesup, R.drawable.cat,
            R.drawable.coolglasses, R.drawable.thumbsupy,
            R.drawable.bike, R.drawable.youreman,
            R.drawable.shoes, R.drawable.iceburn,
            R.drawable.laugh, R.drawable.usa,
            R.drawable.salute, R.drawable.canada,
            R.drawable.uk, R.drawable.twisted,
            R.drawable.dog, R.drawable.portugal,
            R.drawable.estonia, R.drawable.finland,
            R.drawable.csa, R.drawable.quebec,
            R.drawable.rigcon, R.drawable.sonic,
            R.drawable.toot, R.drawable.trophy2,
            R.drawable.cool, R.drawable.dope,
    };

    private String[] mEmoteCodes = {
            ":thumbsup:", "'<",
            ":ateam:", ":brbox:",
            ":shaq:", ":trophy:",
            ":bump:", ":dare:",
            ":texan:", ":lolwut:",
            ":mj:", ":ngage:",
            ":mc:", ":rocky:",
            ":sslogo:", ":winner:",
            ":ballin:", ":pika:",
            ":barneyclap:", ":barneykiss:",
            ":facepalm:", ":unhappy:",
            ":volcanicity:", ":kawaii:",
            ":russian:", ":headbang:",
            ":running:", ":mrtwinner:",
            ":timesup:", "(@)",
            "(H)", "(Y)",
            ":bike:", ":youreman:",
            ":shoes:", ":iceburn:",
            ":laugh:", ":usa:",
            ":salute:", ":canada:",
            ":uk:", ":twisted:",
            ":dog:", ":portugal:",
            ":estonia:", ":finland:",
            ":csa:", ":quebec:",
            ":rigcon:", ":sonic:",
            ":toot:", ":trophy2:",
            ":cool:", ":dope:",
    };

    public EmoteGridAdapter(Context c) {
        mContext = c;
    }

    @Override
    public int getCount() {
        return mEmoteIds.length;
    }

    @Override
    public Object getItem(int position) {
        return mEmoteCodes[position];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(90, 90));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mEmoteIds[position]);
        return imageView;
    }
}
