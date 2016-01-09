package com.yourewinner.yourewinner;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class PostAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    ArrayList<Object> mPosts;

    public PostAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mPosts = new ArrayList<Object>();
    }

    public void updateData(Object[] data) {
        //mPosts = new ArrayList<Object>(Arrays.asList(data));
        mPosts.addAll(Arrays.asList(data));
        notifyDataSetChanged();
    }

    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPosts.size();
    }

    @Override
    public Object getItem(int position) {
        int totalCount = getCount();
        if (totalCount == 0 || position > totalCount) {
            return null;
        }

        return mPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_post, null);
            holder = new ViewHolder();
            holder.boardNameTextView = (TextView) convertView.findViewById(R.id.board_name);
            holder.topicTitleTextView = (TextView) convertView.findViewById(R.id.topic_title);
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.avatarImageView = (ImageView) convertView.findViewById(R.id.avatar);
            holder.postContentTextView = (TextView) convertView.findViewById(R.id.post_content);
            holder.postTimeTextView = (TextView) convertView.findViewById(R.id.post_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String,Object> post = (Map<String,Object>) getItem(position);

        try {
            String username = new String((byte[]) post.get("post_author_name"), "UTF-8");
            holder.usernameTextView.setText(username);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            String boardName = new String((byte[]) post.get("forum_name"), "UTF-8");
            holder.boardNameTextView.setText(boardName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            String topicTitle = new String((byte[]) post.get("topic_title"), "UTF-8");
            holder.topicTitleTextView.setText(Html.fromHtml("<strong>" + topicTitle + "</strong>"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            String postContent = new String((byte[]) post.get("short_content"), "UTF-8");
            holder.postContentTextView.setText(postContent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String avatar = (String) post.get("icon_url");
        if (avatar.length() > 0) {
            Picasso.with(mContext).load(avatar).placeholder(R.mipmap.no_avatar).resize(100, 100).transform(new CircleTransform(mContext, false)).into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.mipmap.no_avatar);
        }

        Date then = (Date) post.get("post_time");
        long now = System.currentTimeMillis();

        //long now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        String postTime = DateUtils.getRelativeTimeSpanString(then.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
        holder.postTimeTextView.setText(postTime);

        return convertView;
    }

    private static class ViewHolder {
        public TextView boardNameTextView;
        public TextView topicTitleTextView;
        public TextView usernameTextView;
        public ImageView avatarImageView;
        public TextView postContentTextView;
        public TextView postTimeTextView;
    }
}
