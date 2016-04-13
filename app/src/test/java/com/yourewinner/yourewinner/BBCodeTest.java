package com.yourewinner.yourewinner;

import junit.framework.TestCase;

public class BBCodeTest extends TestCase {

    public void testEmotes() throws Exception {
        final String in = "YOU'RE :stamp: '< :stamp:";
        final String out = BBCodeConverter.process(in);
        final String expected = "YOU&apos;RE <img src=\"stamp\"> <img src=\"pacman\"> <img src=\"stamp\">";
        assertEquals(expected, out);
    }

    public void testBBCodes() throws Exception {
        final String in = "[quote][url=http://yourewinner.com]YOU'RE WINNER ![/url][/quote]";
        final String out = BBCodeConverter.process(in);
        final String expected = "<blockquote><a href=\"http://yourewinner.com\">YOU&apos;RE WINNER !</a></blockquote>";
        assertEquals(expected, out);
    }

}
