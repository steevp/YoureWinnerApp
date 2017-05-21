package com.yourewinner.yourewinner;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yourewinner.yourewinner.wrapper.PrivateMessageWrapper;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrivateMessageAdapter extends BaseAdapter {

    private Context mContext;
    private String mBoxID;
    private ArrayList<PrivateMessageWrapper> mMessages;

    public PrivateMessageAdapter(Context context, String boxID) {
        mContext = context;
        mBoxID = boxID;
        mMessages = new ArrayList<>();
    }

    public PrivateMessageAdapter(Context context, String boxID, ArrayList<PrivateMessageWrapper> messages) {
        mContext = context;
        mBoxID = boxID;
        mMessages = messages;
    }

    public void updateData(Object[] data) {
        for (Object m : data) {
            mMessages.add(new PrivateMessageWrapper(m));
        }
        notifyDataSetChanged();
    }

    public ArrayList<PrivateMessageWrapper> getData() {
        return mMessages;
    }

    public void clearData() {
        mMessages.clear();
        notifyDataSetChanged();
    }

    public void removeItem(PrivateMessageWrapper message) {
        mMessages.remove(message);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public PrivateMessageWrapper getItem(int position) {
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_private_message, parent, false);
            holder = new ViewHolder();
            holder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.subjectTextView = (TextView) convertView.findViewById(R.id.message_subject);
            holder.bodyTextView = (TextView) convertView.findViewById(R.id.message_body);
            holder.timestampTextView = (TextView) convertView.findViewById(R.id.message_timestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PrivateMessageWrapper message = getItem(position);

        String iconUrl = message.getAvatar();
        if (iconUrl.length() > 0) {
            Picasso.with(mContext).load(iconUrl).placeholder(R.drawable.no_avatar).fit().into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.drawable.no_avatar);
        }

        String sender;
        if (mBoxID.equals("sent")) {
            sender = message.getRecipients();
        } else {
            sender = message.getSender();
        }

        holder.usernameTextView.setText(sender);

        String subject = message.getSubject();
        holder.subjectTextView.setText(subject);

        String body = message.getContent();
        holder.bodyTextView.setText(body);

        Date timestamp = message.getSentDate();
        long now = System.currentTimeMillis();
        String sentTime = DateUtils.getRelativeTimeSpanString(timestamp.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
        holder.timestampTextView.setText(sentTime);

        return convertView;
    }

    private static class ViewHolder {
        public CircleImageView avatarImageView;
        public TextView usernameTextView;
        public TextView subjectTextView;
        public TextView bodyTextView;
        public TextView timestampTextView;
    }
}
