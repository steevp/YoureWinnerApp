package com.yourewinner.yourewinner;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.EscapeProcessor;
import org.kefirsf.bb.TextProcessor;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class TopicViewAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mInflater;
    ArrayList<Object> mPosts;
    TextProcessor BBProcessor;
    TextProcessor EmoteProcessor;

    public TopicViewAdapter(Context context, LayoutInflater inflater) {
        mContext = context;
        mInflater = inflater;
        mPosts = new ArrayList<Object>();
        BBProcessor = BBProcessorFactory.getInstance().create();

        EmoteProcessor = new EscapeProcessor(
                new String[][]{
                        {":stamp:", "<img src=\"http://yourewinner.com/Smileys/default/winnervt4.png\">"},
                        {":loser:", "<img src=\"http://yourewinner.com/Smileys/default/loserox7.png\">"},
                        {":roddy:", "<img src=\"http://yourewinner.com/Smileys/default/rroddy.jpg\">"},
                        {":belair:", "<img src=\"http://yourewinner.com/Smileys/default/belair.jpg\">"},
                        {":bidoof:", "<img src=\"http://yourewinner.com/Smileys/default/Bidoof.gif\">"},
                        {":leek:", "<img src=\"http://yourewinner.com/Smileys/default/leekspin.gif\">"},
                        {":bluerig:", "<img src=\"http://yourewinner.com/Smileys/default/bluerigdtforumavatar5rx.jpg\">"},
                        {":stalin:", "<img src=\"http://yourewinner.com/Smileys/default/stalin.gif\">"},
                        {":thumbsup:", "<img src=\"http://yourewinner.com/Smileys/default/thumbs-up.jpg\">"},
                        {"'>", "<img src=\"http://yourewinner.com/Smileys/default/pacman.png\">"},
                        {":ateam:", "<img src=\"http://yourewinner.com/Smileys/default/A-Team.gif\">"},
                        {":brbox:", "<img src=\"http://yourewinner.com/Smileys/default/BR.jpg\">"},
                        {":shaq:", "<img src=\"http://yourewinner.com/Smileys/default/shaq_kw_copy.gif\">"},
                        {":trophy:", "<img src=\"http://yourewinner.com/Smileys/default/BR_emoticon.JPG\">"},
                        {":bump:", "<img src=\"http://yourewinner.com/Smileys/default/bumpun2.png\">"},
                        {":dare:", "<img src=\"http://yourewinner.com/Smileys/default/DARE2.jpg\">"},
                        {":texan:", "<img src=\"http://yourewinner.com/Smileys/default/jrwinner7mz.JPG\">"},
                        {":lolwut:", "<img src=\"http://yourewinner.com/Smileys/default/lolwut.PNG\">"},
                        {":mj:", "<img src=\"http://yourewinner.com/Smileys/default/MJWINNER.gif\">"},
                        {":ngage:", "<img src=\"http://yourewinner.com/Smileys/default/N-GageD.gif\">"},
                        {":mc:", "<img src=\"http://yourewinner.com/Smileys/default/p1354198.gif\">"},
                        {":rocky:", "<img src=\"http://yourewinner.com/Smileys/default/Rocky.gif\">"},
                        {":sslogo:", "<img src=\"http://yourewinner.com/Smileys/default/SS_Emoticon.gif.jpg\">"},
                        {":winner:", "<img src=\"http://yourewinner.com/Smileys/default/winnerscrooll3vb.gif\">"},
                        {":ballin:", "<img src=\"http://yourewinner.com/Smileys/default/ballin.gif\">"},
                        {":pika:", "<img src=\"http://yourewinner.com/Smileys/default/pika.png\">"},
                        {":barneyclap:", "<img src=\"http://yourewinner.com/Smileys/default/barneyclap.gif\">"},
                        {":barneykiss:", "<img src=\"http://yourewinner.com/Smileys/default/barneykiss.gif\">"},
                        {":facepalm:", "<img src=\"http://yourewinner.com/Smileys/default/facepalm.jpg\">"},
                        {":unhappy:", "<img src=\"http://yourewinner.com/Smileys/default/unhappy.jpg\">"},
                        {":volcanicity:", "<img src=\"http://yourewinner.com/Smileys/default/george.gif\">"},
                        {":kawaii:", "<img src=\"http://yourewinner.com/Smileys/default/konata.gif\">"},
                        {":russian:", "<img src=\"http://yourewinner.com/Smileys/default/RUSSIA.png\">"},
                        {":headbang:", "<img src=\"http://yourewinner.com/Smileys/default/longhairni3.gif\">"},
                        {":running:", "<img src=\"http://yourewinner.com/Smileys/default/kith.gif\">"},
                        {":mrtwinner:", "<img src=\"http://yourewinner.com/Smileys/default/mrtwinner.png\">"},
                        {":timesup:", "<img src=\"http://yourewinner.com/Smileys/default/timesup_stamp.png\">"},
                        {"(@)", "<img src=\"http://yourewinner.com/Smileys/default/.png\">"},
                        {"(H)", "<img src=\"http://yourewinner.com/Smileys/default/H.png\">"},
                        {"(Y)", "<img src=\"http://yourewinner.com/Smileys/default/Y.png\">"},
                        {":bike:", "<img src=\"http://yourewinner.com/Smileys/default/bike.gif\">"},
                        {":youreman:", "<img src=\"http://yourewinner.com/Smileys/default/Youaretheman.gif\">"},
                        {":shoes:", "<img src=\"http://yourewinner.com/Smileys/default/shoes.gif\">"},
                        {":iceburn:", "<img src=\"http://yourewinner.com/Smileys/default/emot-iceburn.gif\">"},
                        {":laugh:", "<img src=\"http://yourewinner.com/Smileys/default/laugh_small.jpg\">"},
                        {":usa:", "<img src=\"http://yourewinner.com/Smileys/default/flageagusa.gif\">"},
                        {":salute:", "<img src=\"http://yourewinner.com/Smileys/default/salute_small.jpg\">"},
                        {":canada:", "<img src=\"http://yourewinner.com/Smileys/default/canada.gif\">"},
                        {":uk:", "<img src=\"http://yourewinner.com/Smileys/default/corgi.gif\">"},
                        {":twisted:", "<img src=\"http://yourewinner.com/Smileys/default/icon_twisted.gif\">"},
                        {":dog:", "<img src=\"http://yourewinner.com/Smileys/default/dancingdog79.gif\">"},
                        {":portugal:", "<img src=\"http://yourewinner.com/Smileys/default/portugal.gif\">"},
                        {":estonia:", "<img src=\"http://yourewinner.com/Smileys/default/estonia.gif\">"},
                        {":finland:", "<img src=\"http://yourewinner.com/Smileys/default/finland.gif\">"},
                        {":csa:", "<img src=\"http://yourewinner.com/Smileys/default/csa.gif\">"},
                        {":quebec:", "<img src=\"http://yourewinner.com/Smileys/default/quebec.gif\">"},
                        {":rigcon:", "<img src=\"http://yourewinner.com/Smileys/default/fistbump_emote.gif\">"},
                        {":sonic:", "<img src=\"http://yourewinner.com/Smileys/default/ChristianSonicCry.gif\">"},
                        {":toot:", "<img src=\"http://yourewinner.com/Smileys/default/toot.gif\">"},
                        {":trophy2:", "<img src=\"http://yourewinner.com/Smileys/default/yourewiener.gif\">"},
                        {":cool:", "<img src=\"http://yourewinner.com/Smileys/default/cool.jpg\">"},
                        {":dope:", "<img src=\"http://yourewinner.com/Smileys/default/dope.gif\">"},
                }
        );
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
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_topic_view, null);
            holder = new ViewHolder();
            holder.usernameTextView = (TextView) convertView.findViewById(R.id.username);
            holder.usernameImageView = (ImageView) convertView.findViewById(R.id.image_username);
            holder.avatarImageView = (CircleImageView) convertView.findViewById(R.id.avatar);
            holder.postContentTextView = (LinearLayout) convertView.findViewById(R.id.post_content);
            holder.postTimeTextView = (TextView) convertView.findViewById(R.id.post_time);
            holder.ratingBar = (LinearLayout) convertView.findViewById(R.id.rating_bar);

            holder.avatarImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //String username = (String) holder.avatarImageView.getTag();
                    String username = holder.usernameTextView.getText().toString();
                    //Toast.makeText(mContext, username + " clicked!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, ProfileViewActivity.class);
                    intent.putExtra("username", username);
                    mContext.startActivity(intent);
                }
            });

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
                        break;
                }

                ratingCount.setText(count + "x");

                holder.ratingBar.addView(ratingCount);
                holder.ratingBar.addView(ratingImage);

            }

        }

        try {
            String username = new String((byte[]) post.get("post_author_name"), "UTF-8");
            //holder.avatarImageView.setTag(username);
            String imageUsername = post.get("image_username").toString();
            if (imageUsername.length() > 0) {
                holder.usernameTextView.setVisibility(View.GONE);
                holder.usernameImageView.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(imageUsername).into(holder.usernameImageView);
                holder.usernameTextView.setText(username);
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
            String[] postContentSplit = postContent.split("(?=\\[img\\])|(?<=\\[/img\\])");
            holder.postContentTextView.removeAllViews();
            Pattern r = Pattern.compile("\\[img\\](.+)\\[/img\\]");
            Matcher m = null;
            ImageView imageView = null;
            LinkifyTextView textView = null;
            for (int i=0;i<postContentSplit.length;i++) {
                m = r.matcher(postContentSplit[i]);
                if (m.find()) {
                    final String imageURL = m.group(1);
                    imageView = new ImageView(mContext);
                    imageView.setScaleType(ImageView.ScaleType.FIT_START);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ViewPhotoActivity.class);
                            intent.putExtra("imageURL", imageURL);
                            mContext.startActivity(intent);
                        }
                    });
                    Glide.with(mContext).load(imageURL).into(imageView);
                    holder.postContentTextView.addView(imageView);
                } else {
                    textView = new LinkifyTextView(mContext);
                    textView.setTextSize(18);
                    textView.setText(Html.fromHtml(EmoteProcessor.process(BBProcessor.process(postContentSplit[i])), new PicassoImageGetter(textView, mContext.getResources(), Picasso.with(mContext)), null));
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    holder.postContentTextView.addView(textView);
                }
            }
            //holder.postContentTextView.setText(Html.fromHtml(EmoteProcessor.process(BBProcessor.process(postContent)), new PicassoImageGetter(holder.postContentTextView, mContext.getResources(), Picasso.with(mContext)), null));
            //holder.postContentTextView.setMovementMethod(LinkMovementMethod.getInstance());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        boolean loggedIn = (boolean) post.get("is_online");

        String avatar = (String) post.get("icon_url");
        if (avatar.length() > 0) {
            //Picasso.with(mContext).load(avatar).placeholder(R.mipmap.no_avatar).transform(new CircleTransform(mContext, loggedIn)).into(holder.avatarImageView);
            Picasso.with(mContext).load(avatar).placeholder(R.mipmap.no_avatar).fit().into(holder.avatarImageView);
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
        public CircleImageView avatarImageView;
        public LinearLayout postContentTextView;
        public TextView postTimeTextView;
        public LinearLayout ratingBar;
    }
}
