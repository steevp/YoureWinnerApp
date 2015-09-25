package com.yourewinner.yourewinner;

import android.content.Context;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class TopicViewAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    ArrayList<Object> mPosts;
    TextProcessor BBProcessor;

    public TopicViewAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mPosts = new ArrayList<Object>();
        BBProcessor = BBProcessorFactory.getInstance().create();

    }

    public void updateData(Object[] data) {
        mPosts = new ArrayList<Object>(Arrays.asList(data));
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
            convertView = mInflater.inflate(R.layout.row_topic_view, null);
            holder = new ViewHolder();
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.usernameImageView = (ImageView) convertView.findViewById(R.id.image_username);
            holder.avatarImageView = (ImageView) convertView.findViewById(R.id.avatar);
            holder.postContentTextView = (LinkifyTextView) convertView.findViewById(R.id.post_content);
            //holder.postContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
            holder.postTimeTextView = (TextView) convertView.findViewById(R.id.post_time);
            holder.ratingBar = (LinearLayout) convertView.findViewById(R.id.rating_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Map<String,Object> post = (Map<String,Object>) getItem(position);

        holder.ratingBar.removeAllViews();

        Object[] ratings = (Object[]) post.get("ratings");
        if (ratings != null && ratings.length > 0) {

            Map<String,Object> rating;
            int count;
            int ratingId;

            TextView ratingCount;
            ImageView ratingImage;

            final float scale = mContext.getResources().getDisplayMetrics().density;
            int pixels = (int) (16 * scale + 0.5f);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(pixels, pixels);

            for (int i=0;i<ratings.length;i++) {
                rating = (Map<String,Object>) ratings[i];
                count = (int) rating.get("count");
                ratingId = (int) rating.get("rate_id");

                ratingCount = new TextView(mContext);
                ratingImage = new ImageView(mContext);
                ratingImage.setLayoutParams(layoutParams);

                switch(ratingId) {
                    case 23:
                        ratingImage.setImageResource(R.mipmap.rating_trophy);
                        break;
                    case 22:
                        ratingImage.setImageResource(R.mipmap.rating_thumbup);
                        break;
                    case 21:
                        ratingImage.setImageResource(R.mipmap.rating_thumbdown);
                        break;
                    case 25:
                        ratingImage.setImageResource(R.mipmap.rating_turd);
                        break;
                    case 26:
                        ratingImage.setImageResource(R.mipmap.rating_gay);
                        break;
                    case 20:
                        ratingImage.setImageResource(R.mipmap.rating_heart);
                        break;
                    case 19:
                        ratingImage.setImageResource(R.mipmap.rating_smug);
                        break;
                    case 24:
                        ratingImage.setImageResource(R.mipmap.rating_cheese);
                        break;
                    case 101:
                        ratingImage.setImageResource(R.mipmap.rating_aggro);
                        break;
                    case 18:
                        ratingImage.setImageResource(R.mipmap.rating_twisted);
                        break;
                    case 17:
                        ratingImage.setImageResource(R.mipmap.rating_zigsd);
                        break;
                    case 16:
                        ratingImage.setImageResource(R.mipmap.rating_old);
                        break;
                    case 102:
                        ratingImage.setImageResource(R.mipmap.rating_baby);
                        break;
                    case 104:
                        ratingImage.setImageResource(R.mipmap.rating_weed);
                        break;
                    case 103:
                        ratingImage.setImageResource(R.mipmap.rating_meta);
                        break;
                    case 111:
                        ratingImage.setImageResource(R.mipmap.rating_ballin);
                        break;
                    case 116:
                        ratingImage.setImageResource(R.mipmap.rating_coorslight);
                        break;
                    case 117:
                        ratingImage.setImageResource(R.mipmap.rating_america);
                        break;
                    case 118:
                        ratingImage.setImageResource(R.mipmap.rating_hipster);
                        break;
                    case 119:
                        ratingImage.setImageResource(R.mipmap.rating_scroogled);
                        break;
                    case 120:
                        ratingImage.setImageResource(R.mipmap.rating_mistletoe);
                        break;
                    default:
                        ratingImage.setImageResource(R.mipmap.ic_launcher);
                        break;
                }

                ratingCount.setText(count + "x");

                holder.ratingBar.addView(ratingCount);
                holder.ratingBar.addView(ratingImage);

            }

        }

        try {
            String username = new String((byte[]) post.get("post_author_name"), "UTF-8");
            String imageUsername = post.get("image_username").toString();
            if (imageUsername.length() > 0) {
                holder.usernameTextView.setVisibility(View.GONE);
                holder.usernameImageView.setVisibility(View.VISIBLE);
                Picasso.with(mContext).load(imageUsername).into(holder.usernameImageView);
            } else {
                holder.usernameTextView.setVisibility(View.VISIBLE);
                holder.usernameImageView.setVisibility(View.GONE);
                holder.usernameTextView.setText(username);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            String postContent = new String((byte[]) post.get("post_content"), "UTF-8");
            //holder.postContentTextView.setText(Html.fromHtml(BBProcessor.process(postContent)));
            //URLImageParser p = new URLImageParser(holder.postContentTextView, mContext);
            //Spanned htmlSpan = Html.fromHtml(BBProcessor.process(postContent), p, null);
            holder.postContentTextView.setText(Html.fromHtml(BBProcessor.process(postContent), new PicassoImageGetter(holder.postContentTextView, mContext.getResources(), Picasso.with(mContext)), null));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String avatar = (String) post.get("icon_url");
        if (avatar.length() > 0) {
            Picasso.with(mContext).load(avatar).placeholder(R.mipmap.no_avatar).transform(new CircleTransform()).into(holder.avatarImageView);
        } else {
            holder.avatarImageView.setImageResource(R.mipmap.no_avatar);
        }

        Date then = (Date) post.get("post_time");
        long now = System.currentTimeMillis();
        String postTime = DateUtils.getRelativeTimeSpanString(then.getTime(), now, DateUtils.MINUTE_IN_MILLIS).toString();

        holder.postTimeTextView.setText(postTime);

        return convertView;
    }

    private static class ViewHolder {
        public TextView usernameTextView;
        public ImageView usernameImageView;
        public ImageView avatarImageView;
        public LinkifyTextView postContentTextView;
        public TextView postTimeTextView;
        public LinearLayout ratingBar;
    }
}
