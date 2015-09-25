package com.yourewinner.yourewinner;

import java.net.MalformedURLException;
import java.net.URL;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCClient;

public class Forum {
    private static Forum ourInstance = new Forum();

    private final static String API_URL = "http://yourewinner.com/winnerapi/mobiquo.php";
    private final static int PAGE_SIZE = 15;
    private XMLRPCClient client;
    private Boolean isLoggedIn;

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
        Object[] params = {username.getBytes(), password.getBytes()};
        client.callAsync(listener, "login", params);
    }

    public void setLogin(Boolean login) {
        isLoggedIn = login;
    }

    public Boolean getLogin() {
        return isLoggedIn;
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
}
