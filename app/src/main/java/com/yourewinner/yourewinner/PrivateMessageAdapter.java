package com.yourewinner.yourewinner;

import android.content.Context;
import android.text.TextUtils;
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

public class PrivateMessageAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private String mBoxID;
    private ArrayList<Object> mMessages;

    public PrivateMessageAdapter(Context context, LayoutInflater inflater, String boxID) {
        mContext = context;
        mInflater = inflater;
        mBoxID = boxID;
        mMessages = new ArrayList<Object>();
    }

    public void updateData(Object[] data) {
        if (data != null) {
            mMessages.addAll(Arrays.asList(data));
            notifyDataSetChanged();
        }
    }

    public Object[] getData() {
        return mMessages.toArray();
    }

    public void clearData() {
        mMessages.clear();
        notifyDataSetChanged();
    }

    public void removeItem(Object object) {
        mMessages.remove(object);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int position) {
        int totalCount = getCount();
        if (totalCount == 0 || position >= totalCount || position < 0) {
            return null;
        }
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_private_message, null);
            holder = new ViewHolder();
            holder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.subjectTextView = (TextView) convertView.findViewById(R.id.message_subject);
            holder.bodyTextview = (TextView) convertView.findViewById(R.id.message_body);
            holder.timestampTextView = (TextView) convertView.findViewById(R.id.message_timestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String,Object> message = (Map<String,Object>) getItem(position);

        String iconUrl = (String) message.get("icon_url");
        if (iconUrl.length() > 0) {
            Picasso.with(mContext).load(iconUrl).placeholder(R.drawable.no_avatar).fit().into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.drawable.no_avatar);
        }

        String sender;
        if (mBoxID.equals("sent")) {
            Object[] msgTo = (Object[]) message.get("msg_to");
            ArrayList<String> senders = new ArrayList<String>();
            for (int i=0;i<msgTo.length;i++) {
                Map<String,Object> map = (Map<String,Object>) msgTo[i];
                senders.add(new String((byte[]) map.get("username"), Charset.forName("UTF-8")));
            }
            sender = TextUtils.join(", ", senders);
        } else {
            sender = new String((byte[]) message.get("msg_from"), Charset.forName("UTF-8"));
        }

        holder.usernameTextView.setText(sender);

        String subject = new String((byte[]) message.get("msg_subject"), Charset.forName("UTF-8"));
        holder.subjectTextView.setText(subject);

        String body = new String((byte[]) message.get("short_content"), Charset.forName("UTF-8"));
        holder.bodyTextview.setText(body);

        Date timestamp = (Date) message.get("sent_date");
        long now = System.currentTimeMillis();
        String sentTime = DateUtils.getRelativeTimeSpanString(timestamp.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
        holder.timestampTextView.setText(sentTime);

        return convertView;
    }

    private static class ViewHolder {
        public CircleImageView avatarImageView;
        public TextView usernameTextView;
        public TextView subjectTextView;
        public TextView bodyTextview;
        public TextView timestampTextView;
    }
}
