package com.blueconic.browscap;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.blueconic.browscap.impl.UserAgentFileParser;

/**
 * Service that manages the creation of user agent parsers. In the feature, this might be expanded so it also supports
 * auto-updating or use a given InputStream
 */
public class UserAgentService {
    // The version of the browscap file this bundle depends on
    private static final int BUNDLED_BROWSCAP_VERSION = 6022;

    /**
     * Returns a parser based on the bundled BrowsCap version
     * @return the user agent parser
     */
    public UserAgentParser loadParser() throws IOException, ParseException {
        // http://browscap.org/version-number
        final String csvFileName = "browscap-" + BUNDLED_BROWSCAP_VERSION + ".zip";
        try (final InputStream zipStream = getClass().getClassLoader().getResourceAsStream(csvFileName);
                final ZipInputStream zipIn = new ZipInputStream(zipStream)) {
            final ZipEntry entry = zipIn.getNextEntry();
            if (!entry.isDirectory()) {
                return new UserAgentFileParser().parse(new InputStreamReader(zipIn, UTF_8));
            } else {
                throw new IOException("Unable to find BrowsCap entry: " + csvFileName);
            }
        }

    }
}
