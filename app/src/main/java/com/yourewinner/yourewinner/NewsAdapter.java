package com.yourewinner.yourewinner;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

import java.util.ArrayList;

/**
 * Created by steven on 3/18/16.
 */
public class NewsAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mNews;
    private TextProcessor mProcessor;

    public NewsAdapter(Context context, LayoutInflater inflater, ArrayList<String> news) {
        mContext = context;
        mInflater = inflater;
        mNews = news;
        mProcessor = BBProcessorFactory.getInstance().create();
    }

    @Override
    public int getCount() {
        return mNews.size();
    }

    @Override
    public Object getItem(int position) {
        return mNews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.news_list_item, null);
            holder = new ViewHolder();
            holder.newsItem = (TextView) convertView.findViewById(R.id.news_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String news = (String) getItem(position);

        // TODO: ugh nvm
        news = mProcessor.process(news)
                .replace("[d]", "<font color='#9C9C9C'><small><strong>[")
                .replace("[/d]", "]</strong></small></font>")
                .replace("&amp;#039;", "'")
                .replace("&amp;nbsp;", " ")
                .replace("&amp;quot;", "\"")
                .replaceAll("\\s(http[^\\s]+)", " <a href='$1'>$1</a>");

        holder.newsItem.setText(Html.fromHtml(news));
        holder.newsItem.setMovementMethod(LinkMovementMethod.getInstance());

        return convertView;
    }

    private static class ViewHolder {
        public TextView newsItem;
    }
}
