package com.blueconic.browscap;

public interface UserAgentParser {

    /**
     * Parses a User-Agent header value into a Capabilities object.
     * @param userAgent The user agent
     * @return The capabilities of the best matching rule
     */
    Capabilities parse(String userAgent);
}
