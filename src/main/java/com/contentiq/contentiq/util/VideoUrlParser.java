package com.contentiq.contentiq.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VideoUrlParser {

    private static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{11}$");

    private static final Pattern SHORT_HOST = Pattern.compile("^(www\\.)?youtu\\.be$");
    private static final Pattern LONG_HOST = Pattern.compile("^(www\\.|m\\.)?youtube\\.com$");

    private VideoUrlParser() {
    }

    public static String extractVideoId(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("URL or video id must not be blank");
        }
        String trimmed = input.trim();

        if (ID_PATTERN.matcher(trimmed).matches()) {
            return trimmed;
        }

        try {
            URI uri = new URI(trimmed);
            String host = uri.getHost();
            if (host == null) {
                throw new IllegalArgumentException("Not a valid YouTube URL: " + input);
            }

            if (SHORT_HOST.matcher(host).matches()) {
                String path = uri.getPath();
                if (path != null && path.length() > 1) {
                    String candidate = path.substring(1);
                    if (ID_PATTERN.matcher(candidate).matches()) {
                        return candidate;
                    }
                }
            }

            if (LONG_HOST.matcher(host).matches()) {
                String path = uri.getPath() == null ? "" : uri.getPath();
                String query = uri.getQuery();

                if ("/watch".equals(path) && query != null) {
                    String v = extractQueryParam(query, "v");
                    if (v != null && ID_PATTERN.matcher(v).matches()) {
                        return v;
                    }
                }

                Matcher m = Pattern.compile("^/(embed|v|shorts)/([A-Za-z0-9_-]{11})").matcher(path);
                if (m.find()) {
                    return m.group(2);
                }
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid YouTube URL: " + input, e);
        }

        throw new IllegalArgumentException("Could not extract video id from: " + input);
    }

    private static String extractQueryParam(String query, String key) {
        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0 && pair.substring(0, idx).equals(key)) {
                return pair.substring(idx + 1);
            }
        }
        return null;
    }
}
