package com.yourewinner.yourewinner;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

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
        if (data != null) {
            mPosts.addAll(Arrays.asList(data));
            notifyDataSetChanged();
        }
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
        if (totalCount == 0 || position >= totalCount || position < 0) {
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
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_post, null);
            holder = new ViewHolder();
            holder.boardNameTextView = (TextView) convertView.findViewById(R.id.board_name);
            holder.topicTitleTextView = (TextView) convertView.findViewById(R.id.topic_title);
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.postContentTextView = (TextView) convertView.findViewById(R.id.post_content);
            holder.postTimeTextView = (TextView) convertView.findViewById(R.id.post_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String,Object> post = (Map<String,Object>) getItem(position);


        byte[] userBytes = post.get("post_author_name") != null ? (byte[]) post.get("post_author_name") : (byte[]) post.get("topic_author_name");
        String username = new String(userBytes, Charset.forName("UTF-8"));
        holder.usernameTextView.setText(username);

        String boardName = new String((byte[]) post.get("forum_name"), Charset.forName("UTF-8"));
        holder.boardNameTextView.setText(boardName);

        String topicTitle = new String((byte[]) post.get("topic_title"), Charset.forName("UTF-8"));
        holder.topicTitleTextView.setText(Html.fromHtml("<strong>" + topicTitle + "</strong>"));

        String postContent = new String((byte[]) post.get("short_content"), Charset.forName("UTF-8"));
        holder.postContentTextView.setText(postContent);

        String avatar = (String) post.get("icon_url");
        if (avatar.length() > 0) {
            Picasso.with(mContext).load(avatar).placeholder(R.drawable.no_avatar).fit().into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.drawable.no_avatar);
        }

        Date then = post.get("post_time") != null ? (Date) post.get("post_time") : (Date) post.get("last_reply_time");
        long now = System.currentTimeMillis();

        String postTime = DateUtils.getRelativeTimeSpanString(then.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
        holder.postTimeTextView.setText(postTime);

        return convertView;
    }

    private static class ViewHolder {
        public TextView boardNameTextView;
        public TextView topicTitleTextView;
        public TextView usernameTextView;
        public CircleImageView avatarImageView;
        public TextView postContentTextView;
        public TextView postTimeTextView;
    }
}
