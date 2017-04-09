package com.yourewinner.yourewinner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by steven on 4/4/17.
 */

public class PostsAdapterRv extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static int TYPE_ITEM = 0;
    private final static int TYPE_FOOTER = 1;

    private Context mContext;
    private List<Object> mDataSet;
    private OnItemClickedListener mCallback;
    private boolean mShowFooter;

    public interface OnItemClickedListener {
        public void onItemClicked(Map<String, Object> item);
    }

    // Holds a reference to each view
    public static class VHItem extends RecyclerView.ViewHolder {
        public TextView mBoardName;
        public TextView mTopicTitle;
        public TextView mUsername;
        public CircleImageView mAvatar;
        public TextView mPostContent;
        public TextView mDate;

        public VHItem(View itemView) {
            super(itemView);
            mBoardName = (TextView) itemView.findViewById(R.id.board_name);
            mTopicTitle = (TextView) itemView.findViewById(R.id.topic_title);
            mUsername = (TextView) itemView.findViewById(R.id.username);
            mAvatar = (CircleImageView) itemView.findViewById(R.id.avatar);
            mPostContent = (TextView) itemView.findViewById(R.id.post_content);
            mDate = (TextView) itemView.findViewById(R.id.post_time);
        }

        public void bind(final Map<String, Object> item, final OnItemClickedListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClicked(item);
                }
            });
        }
    }

    // Holds the footer
    public static class VHFooter extends RecyclerView.ViewHolder {

        public VHFooter(View itemView) {
            super(itemView);
        }
    }

    public PostsAdapterRv(Context c, OnItemClickedListener listener) {
        mContext = c;
        mDataSet = new ArrayList<>();
        mCallback = listener;
    }

    public void setFooterEnabled(boolean b) {
        if (!mShowFooter && b) {
            // Show footer
            mShowFooter = true;
            notifyItemInserted(mDataSet.size());
        } else if (mShowFooter && !b){
            // Remove footer
            mShowFooter = false;
            notifyItemRemoved(mDataSet.size());
        }
    }

    public void updateData(Object[] data) {
        int posStart = getItemCount();
        int itemCount = data.length;
        mDataSet.addAll(Arrays.asList(data));
        notifyItemRangeInserted(posStart, itemCount);
    }

    public void clear() {
        int itemCount = getItemCount();
        mDataSet.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == TYPE_ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false);
            return new VHItem(v);
        } else if (viewType == TYPE_FOOTER) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading, parent, false);
            return new VHFooter(v);
        }

        throw new RuntimeException("Unknown View Type: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof VHItem) {
            VHItem vhItem = (VHItem) holder;

            @SuppressWarnings("unchecked")
            Map<String, Object> post = (Map<String, Object>) mDataSet.get(position);

            vhItem.bind(post, mCallback);

            // Set username
            byte[] userBytes;
            if (post.get("post_author_name") != null) {
                userBytes = (byte[]) post.get("post_author_name");
            } else {
                userBytes = (byte[]) post.get("topic_author_name");
            }
            String username = new String(userBytes, Charset.forName("UTF-8"));
            vhItem.mUsername.setText(username);

            // Set board name
            String boardName = new String((byte[]) post.get("forum_name"), Charset.forName("UTF-8"));
            vhItem.mBoardName.setText(boardName);

            // Set topic title
            String topicTitle = new String((byte[]) post.get("topic_title"), Charset.forName("UTF-8"));
            vhItem.mTopicTitle.setText(topicTitle);

            // Set post content
            String postContent = new String((byte[]) post.get("short_content"), Charset.forName("UTF-8"));
            vhItem.mPostContent.setText(postContent);

            // Set avatar
            String avatar = (String) post.get("icon_url");
            if (avatar.length() > 0) {
                Picasso.with(mContext).load(avatar).placeholder(R.drawable.no_avatar).fit().into(vhItem.mAvatar);
            } else {
                vhItem.mAvatar.setImageResource(R.drawable.no_avatar);
            }

            // Set post time
            Date then;
            if (post.get("post_time") != null) {
                then = (Date) post.get("post_time");
            } else {
                then = (Date) post.get("last_reply_time");
            }
            long now = System.currentTimeMillis();
            String postTime = DateUtils.getRelativeTimeSpanString(then.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();
            vhItem.mDate.setText(postTime);
        } else if (holder instanceof VHFooter) {
            VHFooter vhFooter = (VHFooter) holder;
        }
    }

    @Override
    public int getItemCount() {
        if (mShowFooter) {
            return mDataSet.size() + 1;
        }
        return mDataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mDataSet.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }
}
