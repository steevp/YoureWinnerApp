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
        try {
            client = new XMLRPCClient(new URL(API_URL), XMLRPCClient.FLAGS_ENABLE_COOKIES);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void login(String username, String password, XMLRPCCallback listener) {
        mUsername = username;
        Object[] params = {username.getBytes(), password.getBytes()};
        client.callAsync(listener, "login", params);
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

    public void getRecent(int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {start, end};
        client.callAsync(listener, "get_latest_topic", params);
    }

    public void getUnread(int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {start, end};
        client.callAsync(listener, "get_unread_topic", params);
    }

    public void getSubscribed(int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {start, end};
        client.callAsync(listener, "get_subscribed_topic", params);
    }

    public void getTopic(String topic, int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {topic, start, end};
        client.callAsync(listener, "get_thread", params);
    }

    public void getTopicByUnread(String topic, XMLRPCCallback listener) {
        Object[] params = {topic, PAGE_SIZE};
        client.callAsync(listener, "get_thread_by_unread", params);
    }

    public void replyPost(String board, String topic, String subject, String message, XMLRPCCallback listener) {
        Object[] params = {board, topic, subject.getBytes(), message.getBytes()};
        client.callAsync(listener, "reply_post", params);
    }

    public void markAllAsRead(XMLRPCCallback listener) {
        client.callAsync(listener, "mark_all_as_read");
    }

    public void ratePost(String postID, String ratingID, XMLRPCCallback listener) {
        Object[] params = {postID, ratingID};
        client.callAsync(listener, "rate_post", params);
    }

    public void getRawPost(String postID, XMLRPCCallback listener) {
        client.callAsync(listener, "get_raw_post", postID);
    }

    public void saveRawPost(String postID, String postTitle, String postContent, XMLRPCCallback listener) {
        Object[] params = {postID, postTitle.getBytes(), postContent.getBytes()};
        client.callAsync(listener, "save_raw_post", params);
    }

    public void getQuotePost(String postID, XMLRPCCallback listener) {
        client.callAsync(listener, "get_quote_post", postID);
    }

    public void subscribeTopic(String topicID, XMLRPCCallback listener) {
        client.callAsync(listener, "subscribe_topic", topicID);
    }

    public void unsubscribeTopic(String topicID, XMLRPCCallback listener) {
        client.callAsync(listener, "unsubscribe_topic", topicID);
    }

    public void getUserInfo(String username, XMLRPCCallback listener) {
        Object[] params = {username.getBytes()};
        client.callAsync(listener, "get_user_info", params);
    }

    public void getForum(XMLRPCCallback listener) {
        client.callAsync(listener, "get_forum");
    }

    public void getBoard(String boardID, int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {boardID, start, end};
        client.callAsync(listener, "get_topic", params);
    }

    public void searchTopic(String search, int page, XMLRPCCallback listener) {
        /*int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {search.getBytes(), start, end};
        client.callAsync(listener, "search_topic", params);*/

        Map<String,Object> args = new HashMap<String,Object>();
        args.put("page", page);
        args.put("perpage", PAGE_SIZE);
        args.put("keywords", search.getBytes());
        client.callAsync(listener, "search", args);
    }

    public void getBox(String boxID, int page, XMLRPCCallback listener) {
        // boxID can be "inbox", "sent", or "unread"
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {boxID, start, end};
        client.callAsync(listener, "get_box", params);
    }

    public void getMessage(String msgID, String boxID, XMLRPCCallback listener) {
        Object[] params = {msgID, boxID};
        client.callAsync(listener, "get_message", params);
    }

    public void getQuotePM(String msgID, XMLRPCCallback listener) {
        client.callAsync(listener, "get_quote_pm", msgID);
    }

    public void createMessage(String[] recipients, String subject, String body, XMLRPCCallback listener) {
        Object[] to = new Object[recipients.length];
        for (int i=0;i<recipients.length;i++) {
            to[i] = recipients[i].getBytes();
        }

        Object[] params = {to, subject.getBytes(), body.getBytes()};
        client.callAsync(listener, "create_message", params);
    }

    public boolean deleteMessage(String msgID, String boxID) {
        Object[] params = {msgID, boxID};
        try {
            Map<String,Object> r = (Map<String,Object>) client.call("delete_message", params);
            boolean result = (boolean) r.get("result");
            return result;
        } catch (XMLRPCException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void getInboxStat(XMLRPCCallback listener) {
        client.callAsync(listener, "get_inbox_stat");
    }

    public void getParticipatedTopic(String username, int page, XMLRPCCallback listener) {
        int start = page * PAGE_SIZE - PAGE_SIZE;
        int end = page * PAGE_SIZE - 1;

        Object[] params = {username.getBytes(), start, end};
        client.callAsync(listener, "get_participated_topic", params);
    }

    public void newTopic(String boardID, String subject, String body, XMLRPCCallback listener) {
        Object[] params = {boardID, subject.getBytes(), body.getBytes()};
        client.callAsync(listener, "new_topic", params);
    }

    public void viewRatings(String msgID, XMLRPCCallback listener) {
        client.callAsync(listener, "view_ratings", msgID);
    }
}
