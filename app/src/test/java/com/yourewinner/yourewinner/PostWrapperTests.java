package com.yourewinner.yourewinner;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PostWrapperTests {

    /**
     * Test equals implementation for PostWrapper
     */
    @Test
    public void equalsWorks() {
        final Map<String,Object> post = createFakeObject();
        PostsWrapper wrapper1 = new PostsWrapper(post);
        PostsWrapper wrapper2 = new PostsWrapper(post);
        assertEquals(wrapper1, wrapper2);
        post.put("topic_id", "345");
        wrapper2 = new PostsWrapper(post);
        assertNotEquals(wrapper1, wrapper2);
    }

    private Map<String,Object> createFakeObject() {
        final Map<String,Object> post = new HashMap<>();
        post.put("topic_id", "123");
        post.put("post_author_name", "steven".getBytes());
        post.put("forum_name", "Social/Off-topic".getBytes());
        post.put("forum_id", "8");
        post.put("topic_title", "Test title".getBytes());
        post.put("short_content", "Test".getBytes());
        post.put("post_time", new Date());
        post.put("icon_url", "http://example.com/avatar.png");
        return post;
    }
}
