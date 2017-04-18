package com.yourewinner.yourewinner;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

public class BBCodeConverter {
    private static TextProcessor mProcessor = BBProcessorFactory.getInstance().create();

    public static String process(String bbcode) {
        bbcode = mProcessor.process(bbcode)
                .replace(":stamp:", getImageTag("stamp"))
                .replace(":loser:", getImageTag("loser"))
                .replace(":roddy:", getImageTag("roddy"))
                .replace(":belair:", getImageTag("belair"))
                .replace(":bidoof:", getImageTag("bidoof"))
                .replace(":leek:", getImageTag("leek"))
                .replace(":bluerig:", getImageTag("bluerig"))
                .replace(":stalin:", getImageTag("stalin"))
                .replace(":thumbsup:", getImageTag("thumbsup"))
                .replace("&apos;&lt;", getImageTag("pacman"))
                .replace(":ateam:", getImageTag("ateam"))
                .replace(":brbox:", getImageTag("brbox"))
                .replace(":shaq:", getImageTag("shaq"))
                .replace(":trophy:", getImageTag("trophy"))
                .replace(":bump:", getImageTag("bump"))
                .replace(":dare:", getImageTag("dare"))
                .replace(":texan:", getImageTag("texan"))
                .replace(":lolwut:", getImageTag("lolwut"))
                .replace(":mj:", getImageTag("mj"))
                .replace(":ngage:", getImageTag("ngage"))
                .replace(":mc:", getImageTag("mc"))
                .replace(":rocky:", getImageTag("rocky"))
                .replace(":sslogo:", getImageTag("sslogo"))
                .replace(":winner:", getImageTag("winner"))
                .replace(":ballin:", getImageTag("ballin"))
                .replace(":pika:", getImageTag("pika"))
                .replace(":barneyclap:", getImageTag("barneyclap"))
                .replace(":barneykiss:", getImageTag("barneykiss"))
                .replace(":facepalm:", getImageTag("facepalm"))
                .replace(":unhappy:", getImageTag("unhappy"))
                .replace(":volcanicity:", getImageTag("volcanicity"))
                .replace(":kawaii:", getImageTag("kawaii"))
                .replace(":russian:", getImageTag("russian"))
                .replace(":headbang:", getImageTag("headbang"))
                .replace(":running:", getImageTag("running"))
                .replace(":mrtwinner:", getImageTag("mrtwinner"))
                .replace(":timesup:", getImageTag("timesup"))
                .replace("(@)", getImageTag("cat"))
                .replace("(H)", getImageTag("coolglasses"))
                .replace("(Y)", getImageTag("thumbsupy"))
                .replace(":bike:", getImageTag("bike"))
                .replace(":youreman:", getImageTag("youreman"))
                .replace(":shoes:", getImageTag("shoes"))
                .replace(":iceburn:", getImageTag("iceburn"))
                .replace(":laugh:", getImageTag("laugh"))
                .replace(":usa:", getImageTag("usa"))
                .replace(":salute:", getImageTag("salute"))
                .replace(":canada:", getImageTag("canada"))
                .replace(":uk:", getImageTag("uk"))
                .replace(":twisted:", getImageTag("twisted"))
                .replace(":dog:", getImageTag("dog"))
                .replace(":portugal:", getImageTag("portugal"))
                .replace(":estonia:", getImageTag("estonia"))
                .replace(":finland:", getImageTag("finland"))
                .replace(":csa:", getImageTag("csa"))
                .replace(":quebec:", getImageTag("quebec"))
                .replace(":rigcon:", getImageTag("rigcon"))
                .replace(":sonic:", getImageTag("sonic"))
                .replace(":toot:", getImageTag("toot"))
                .replace(":trophy2:", getImageTag("trophy2"))
                .replace(":cool:", getImageTag("cool"))
                .replace(":dope:", getImageTag("dope"));

        return bbcode;
    }

    public static String[] split(String text) {
        // Replace youtube links with yt bbcode
        text = text.replaceAll("\\[url=https?://(?:www\\.)?youtu(?:\\.be/|be\\.com/watch\\?v=)([\\w\\-]{11})\\].+\\[/url\\]",
                "[yt]$1[/yt]");
        // Separate text from bbcodes we need to implement
        return text.split("(?=\\[(yt|img|quote|spoiler)\\])|(?<=\\[/(yt|img|quote|spoiler)\\])");
    }

    private static String getImageTag(String src) {
        return "<img src=\"" + src + "\">";
    }
}
