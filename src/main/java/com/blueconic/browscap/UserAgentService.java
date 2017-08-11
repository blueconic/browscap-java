package com.blueconic.browscap;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.blueconic.browscap.impl.UserAgentFileParser;

/**
 * Service that manages the creation of user agent parsers. In the feature, this might be expanded so it also supports
 * auto-updating or use a given InputStream
 */
public class UserAgentService {
    // The version of the browscap file this bundle depends on
    public static final int BUNDLED_BROWSCAP_VERSION = 6023;

    private String zipFilePath;

    public UserAgentService(){
        // Default
    }

    /**
     *
     * @param zipFilePath the zip file should contain the csv file. It will load the given zip file instead of
     *                    the bundled one
     */
    public UserAgentService(String zipFilePath) {
        this.zipFilePath = zipFilePath;
    }



    /**
     * Returns a parser based on the bundled BrowsCap version
     * @return the user agent parser
     */
    public UserAgentParser loadParser() throws IOException, ParseException {
        // http://browscap.org/version-number
        final String csvFileName = getBundledCsvFileName();
        try (final InputStream zipStream = getCsvFileStream();
             final ZipInputStream zipIn = new ZipInputStream(zipStream)) {
            final ZipEntry entry = zipIn.getNextEntry();
            if (!entry.isDirectory()) {
                return new UserAgentFileParser().parse(new InputStreamReader(zipIn, UTF_8));
            } else {
                throw new IOException("Unable to find BrowsCap entry: " + csvFileName);
            }
        }
    }

    private String getBundledCsvFileName() {
        return "browscap-" + BUNDLED_BROWSCAP_VERSION + ".zip";
    }

    private InputStream getCsvFileStream() throws FileNotFoundException {

        if (this.zipFilePath == null) {

            final String csvFileName = getBundledCsvFileName();
            return getClass().getClassLoader().getResourceAsStream(csvFileName);
        } else {

            return new FileInputStream(this.zipFilePath);
        }
    }
}
