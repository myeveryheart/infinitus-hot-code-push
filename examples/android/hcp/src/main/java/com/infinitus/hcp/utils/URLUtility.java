package com.infinitus.hcp.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by M on 16/9/9.
 * <p/>
 * URL工具类
 */
public class URLUtility {

    /**
     * string转URL
     *
     * @param urlString string
     * @return url object
     * @see URL
     */
    public static URL stringToUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(URLDecoder.decode(urlString, "UTF-8"));
        } catch (Exception e) {
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e2) {
                e2.printStackTrace();
            }
        }

        return url;
    }

    /**
     * Construct url from the provided paths.
     * Doesn't support url parameters. Only file path
     *
     * @param urlParts parts of the url
     * @return constructed url
     */
    public static String construct(String... urlParts) {
        if (urlParts == null || urlParts.length == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        String startingPart = removeStartingDash(urlParts[0]);
        startingPart = removeEndingDash(startingPart);
        if (!startingPart.startsWith("http")) {
            builder.append("http://");
        }
        builder.append(startingPart);

        for (int i = 1, len = urlParts.length; i < len; i++) {
            String urlPart = removeEndingDash(urlParts[i]);
            if (!urlPart.startsWith("/")) {
                builder.append("/");
            }

            builder.append(urlPart);
        }

        return builder.toString();
    }

    private static String removeStartingDash(String string) {
        if (string.startsWith("/")) {
            string = removeStartingDash(string.substring(1));
        }

        return string;
    }

    private static String removeEndingDash(String string) {
        if (string.endsWith("/")) {
            string = removeEndingDash(string.substring(0, string.length() - 1));
        }

        return string;
    }
}
