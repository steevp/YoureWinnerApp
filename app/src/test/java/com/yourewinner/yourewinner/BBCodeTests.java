package com.yourewinner.yourewinner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BBCodeTests {

    /**
     * Test Emotes
     */
    @Test
    public void emotesWork() {
        final String in = "YOU'RE :stamp: '< :stamp:";
        final String out = BBCodeConverter.process(in);
        final String expected = "YOU&apos;RE <img src=\"stamp\"> <img src=\"pacman\"> <img src=\"stamp\">";
        assertEquals(expected, out);
    }

    /**
     * Test BBCode
     */
    @Test
    public void bbCodeWorks() {
        final String in = "[quote][url=http://yourewinner.com]YOU'RE WINNER ![/url][/quote]";
        final String out = BBCodeConverter.process(in);
        final String expected = "<blockquote><a href=\"http://yourewinner.com\">YOU&apos;RE WINNER !</a></blockquote>";
        assertEquals(expected, out);
    }

}
