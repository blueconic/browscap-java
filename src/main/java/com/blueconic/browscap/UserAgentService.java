package com.blueconic.browscap;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.blueconic.browscap.impl.UserAgentFileParser;

/**
 * Service that manages the creation of user agent parsers. In the feature, this might be expanded so it also supports
 */
public class UserAgentService {
    // The version of the browscap file this bundle depends on
    public static final int BUNDLED_BROWSCAP_VERSION = 6024;
    private String myZipFilePath;

    public UserAgentService() {
        // Default
    }

    /**
     * Creates a user agent service based on the Browscap CSV file in the given ZIP file
     * @param zipFilePath the zip file should contain the csv file. It will load the given zip file instead of the
     *            bundled one
     */
    public UserAgentService(String zipFilePath) {
        this.myZipFilePath = zipFilePath;
    }

    /**
     * Returns a parser based on the bundled BrowsCap version
     * @return the user agent parser
     */
    public UserAgentParser loadParser() throws IOException, ParseException {
        // http://browscap.org/version-number
        try (final InputStream zipStream = getCsvFileStream();
                final ZipInputStream zipIn = new ZipInputStream(zipStream)) {
            final ZipEntry entry = zipIn.getNextEntry();

            // look for the first file that isn't a directory
            // that should be a BrowsCap .csv file
            if (!entry.isDirectory()) {
                return new UserAgentFileParser().parse(new InputStreamReader(zipIn, UTF_8));
            } else {
                throw new IOException(
                        "Unable to find the BrowsCap CSV file in the ZIP file");
            }
        }
    }

    /**
     * Returns the bundled ZIP file name
     * @return
     */
    private String getBundledCsvFileName() {
        return "browscap-" + BUNDLED_BROWSCAP_VERSION + ".zip";
    }

    /**
     * Returns the InputStream to the CSV file. This is either the bundled ZIP file or the one passed in the
     * constructor.
     * @return
     * @throws FileNotFoundException
     */
    private InputStream getCsvFileStream() throws FileNotFoundException {
        if (this.myZipFilePath == null) {
            final String csvFileName = getBundledCsvFileName();
            return getClass().getClassLoader().getResourceAsStream(csvFileName);
        } else {
            return new FileInputStream(this.myZipFilePath);
        }
    }
}
