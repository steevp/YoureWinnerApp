package com.yourewinner.yourewinner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

public class Forum {
    private static Forum ourInstance = new Forum();

    private final static String API_URL = "http://yourewinner.com/winnerapi/mobiquo.php";
    private final static int PAGE_SIZE = 15;
    private XMLRPCClient client;
    private Boolean isLoggedIn;
    private String mUsername;

    public static Forum getInstance() {
        return ourInstance;
    }

    private Forum() {
        isLoggedIn = false;
        mUsername = "Guest";
        try {
            client = new XMLRPCClient(new URL(API_URL), XMLRPCClient.FLAGS_ENABLE_COOKIES);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public long login(String username, String password, XMLRPCCallback listener) {
        client.clearCookies();
        mUsername = username;
        Object[] params = {username.getBytes(), password.getBytes()};
        return client.callAsync(listener, "login", params);
    }

    public long logout(XMLRPCCallback listener) {
        isLoggedIn = false;
        return client.callAsync(listener, "logout_user");
    }

    public Map<String,String> getCookies() {
        return client.getCookies();
    }

    public void setCookies(Map<String,String> cookies) {
        client.setCookies(cookies);
    }

    public void setLogin(Boolean login) {
        isLoggedIn = login;
    }

    public Boolean getLogin() {
        return isLoggedIn;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void cancel(long id) {
        client.cancel(id);
    }

    public long getRecent(int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {start, end};
        return client.callAsync(listener, "get_latest_topic", params);
    }

    public long getUnread(int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {start, end};
        return client.callAsync(listener, "get_unread_topic", params);
    }

    public long getSubscribed(int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {start, end};
        return client.callAsync(listener, "get_subscribed_topic", params);
    }

    public long getTopic(String topic, int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {topic, start, end};
        return client.callAsync(listener, "get_thread", params);
    }

    public long getTopicByUnread(String topic, XMLRPCCallback listener) {
        Object[] params = {topic, PAGE_SIZE};
        return client.callAsync(listener, "get_thread_by_unread", params);
    }

    public long replyPost(String board, String topic, String subject, String message, XMLRPCCallback listener) {
        Object[] params = {board, topic, subject.getBytes(), message.getBytes()};
        return client.callAsync(listener, "reply_post", params);
    }

    public long markAllAsRead(XMLRPCCallback listener) {
        return client.callAsync(listener, "mark_all_as_read");
    }

    public boolean ratePost(String postID, String ratingID) throws XMLRPCException {
        Object[] params = {postID, ratingID};
        Map<String, Object> r = (Map<String, Object>) client.call("rate_post", params);
        return (boolean) r.get("result");
    }

    public long getRawPost(String postID, XMLRPCCallback listener) {
        return client.callAsync(listener, "get_raw_post", postID);
    }

    public long saveRawPost(String postID, String postTitle, String postContent, XMLRPCCallback listener) {
        Object[] params = {postID, postTitle.getBytes(), postContent.getBytes()};
        return client.callAsync(listener, "save_raw_post", params);
    }

    public long getQuotePost(String postID, XMLRPCCallback listener) {
        return client.callAsync(listener, "get_quote_post", postID);
    }

    public long subscribeTopic(String topicID, XMLRPCCallback listener) {
        return client.callAsync(listener, "subscribe_topic", topicID);
    }

    public long unsubscribeTopic(String topicID, XMLRPCCallback listener) {
        return client.callAsync(listener, "unsubscribe_topic", topicID);
    }

    public long getUserInfo(String username, XMLRPCCallback listener) {
        Object[] params = {username.getBytes()};
        return client.callAsync(listener, "get_user_info", params);
    }

    public long getForum(XMLRPCCallback listener) {
        return client.callAsync(listener, "get_forum");
    }

    public long getBoard(String boardID, int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {boardID, start, end};
        return client.callAsync(listener, "get_topic", params);
    }

    public long searchTopic(String search, int page, String searchUser, boolean titleOnly, XMLRPCCallback listener) {
        Map<String,Object> args = new HashMap<String,Object>();
        args.put("page", page);
        args.put("perpage", PAGE_SIZE);
        args.put("keywords", search.getBytes());
        if (searchUser != null) {
            args.put("searchuser", searchUser.getBytes());
        }
        if (titleOnly) {
            args.put("titleonly", 1);
        }
        // Hide Deleted Posts board
        args.put("not_in", new String[]{"20"});
        return client.callAsync(listener, "search", args);
    }

    public long getBox(String boxID, int page, XMLRPCCallback listener) {
        // boxID can be "inbox", "sent", or "unread"
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {boxID, start, end};
        return client.callAsync(listener, "get_box", params);
    }

    public long getMessage(String msgID, String boxID, XMLRPCCallback listener) {
        Object[] params = {msgID, boxID};
        return client.callAsync(listener, "get_message", params);
    }

    public long getQuotePM(String msgID, XMLRPCCallback listener) {
        return client.callAsync(listener, "get_quote_pm", msgID);
    }

    public long createMessage(String[] recipients, String subject, String body, XMLRPCCallback listener) {
        Object[] to = new Object[recipients.length];
        for (int i=0;i<recipients.length;i++) {
            to[i] = recipients[i].getBytes();
        }

        Object[] params = {to, subject.getBytes(), body.getBytes()};
        return client.callAsync(listener, "create_message", params);
    }

    public boolean deleteMessage(String msgID, String boxID) throws XMLRPCException {
        Object[] params = {msgID, boxID};
        Map<String,Object> r = (Map<String,Object>) client.call("delete_message", params);
        return (boolean) r.get("result");
    }

    public long getInboxStat(XMLRPCCallback listener) {
        return client.callAsync(listener, "get_inbox_stat");
    }

    public long getParticipatedTopic(String username, int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {username.getBytes(), start, end};
        return client.callAsync(listener, "get_participated_topic", params);
    }

    public long newTopic(String boardID, String subject, String body, XMLRPCCallback listener) {
        Object[] params = {boardID, subject.getBytes(), body.getBytes()};
        return client.callAsync(listener, "new_topic", params);
    }

    public long viewRatings(String msgID, XMLRPCCallback listener) {
        return client.callAsync(listener, "view_ratings", msgID);
    }

    public long getNews(XMLRPCCallback listener) {
        return client.callAsync(listener, "get_news");
    }

    public long getMentions(XMLRPCCallback listener) {
        return client.callAsync(listener, "get_mentions");
    }

    public long deletePost(String postID, XMLRPCCallback listener) {
        // 1 = SOFT_DELETE, 2 = HARD_DELETE
        Object[] params = {postID, 1};
        return client.callAsync(listener, "m_delete_post", params);
    }
}
