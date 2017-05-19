package com.yourewinner.yourewinner;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;

public class NewsAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> mNews;

    public NewsAdapter(Context context) {
        mContext = context;
        mNews = new ArrayList<>();
    }

    public NewsAdapter(Context context, ArrayList<String> newsList) {
        mContext = context;
        mNews = newsList;
    }

    @Override
    public int getCount() {
        return mNews.size();
    }

    @Override
    public String getItem(int position) {
        return mNews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView newsItem;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_list_item, parent, false);
            newsItem = (TextView) convertView.findViewById(R.id.news_item);
            convertView.setTag(newsItem);
        } else {
            newsItem = (TextView) convertView.getTag();
        }

        String news = getItem(position);

        // TODO: make this better
        news = BBCodeConverter.process(news)
                .replace("[d]", "<font color='#9C9C9C'><small><strong>[")
                .replace("[/d]", "]</strong></small></font>")
                .replace("&amp;#039;", "'")
                .replace("&amp;nbsp;", " ")
                .replace("&amp;quot;", "\"")
                .replaceAll("\\s(http[^\\s]+)", " <a href='$1'>$1</a>");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            newsItem.setText(Html.fromHtml(news, Html.FROM_HTML_MODE_LEGACY, new EmoteImageGetter(mContext), null));
        } else {
            //noinspection deprecation
            newsItem.setText(Html.fromHtml(news, new EmoteImageGetter(mContext), null));
        }

        newsItem.setMovementMethod(LinkMovementMethod.getInstance());

        return convertView;
    }

    public void updateData(Object[] data) {
        for (Object n : data) {
            final String news = new String((byte[]) n, Charset.forName("UTF-8"));
            mNews.add(news);
        }
        notifyDataSetChanged();
    }

    public ArrayList<String> getData() {
        return mNews;
    }
}
