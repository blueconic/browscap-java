package com.blueconic.browscap;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toSet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.blueconic.browscap.impl.UserAgentFileParser;

/**
 * Service that manages the creation of user agent parsers. In the feature, this might be expanded so it also supports
 */
public class UserAgentService {

    // The version of the browscap file this bundle depends on
    public static final int BUNDLED_BROWSCAP_VERSION = 6026;
    private String myZipFilePath;

    private final boolean isLiteOnly;

    /**
     * Default constructor
     */
    public UserAgentService() {
        this(false);
    }

    /**
     * A constructor that set whether or not to use the lite-only matches.
     * @param isLiteOnly boolean Determine whether or not to use lite-only matches.
     */
    public UserAgentService(boolean isLiteOnly) {
        this.isLiteOnly = isLiteOnly;
    }

    /**
     * Creates a user agent service based on the Browscap CSV file in the given ZIP file
     * @param zipFilePath the zip file should contain the csv file. It will load the given zip file instead of the
     *            bundled one
     */
    public UserAgentService(final String zipFilePath) {
        this(zipFilePath, false);
    }

    /**
     * Creates a user agent service based on the Browscap CSV file in the given ZIP file
     * @param zipFilePath the zip file should contain the csv file. It will load the given zip file instead of the
     *            bundled one
     * @param isLiteOnly boolean Determine whether or not to use lite-only matches.
     */
    public UserAgentService(final String zipFilePath, boolean isLiteOnly) {
        this(isLiteOnly);
        myZipFilePath = zipFilePath;
    }

    /**
     * Returns a parser based on the bundled BrowsCap version
     * @return the user agent parser
     */
    public UserAgentParser loadParser() throws IOException, ParseException {

        // Use all default fields
        final Set<BrowsCapField> defaultFields =
                Stream.of(BrowsCapField.values()).filter(BrowsCapField::isDefault).collect(toSet());

        return createParserWithFields(defaultFields);
    }

    /**
     * Returns a parser based on the bundled BrowsCap version
     * @param fields list
     * @return the user agent parser
     */
    public UserAgentParser loadParser(final Collection<BrowsCapField> fields) throws IOException, ParseException {
        return createParserWithFields(fields);
    }

    private UserAgentParser createParserWithFields(final Collection<BrowsCapField> fields)
            throws IOException, ParseException {
        // http://browscap.org/version-number
        try (final InputStream zipStream = getCsvFileStream();
                final ZipInputStream zipIn = new ZipInputStream(zipStream)) {
            final ZipEntry entry = zipIn.getNextEntry();

            // look for the first file that isn't a directory
            // that should be a BrowsCap .csv file
            if (!entry.isDirectory()) {
                return UserAgentFileParser.parse(new InputStreamReader(zipIn, UTF_8), fields, isLiteOnly);
            } else {
                throw new IOException(
                        "Unable to find the BrowsCap CSV file in the ZIP file");
            }
        }
    }

    /**
     * Returns the bundled ZIP file name
     * @return CSV file name
     */
    public static String getBundledCsvFileName() {
        return "browscap-" + BUNDLED_BROWSCAP_VERSION + ".zip";
    }

    /**
     * Returns the InputStream to the CSV file. This is either the bundled ZIP file or the one passed in the
     * constructor.
     * @return
     * @throws FileNotFoundException
     */
    private InputStream getCsvFileStream() throws FileNotFoundException {
        if (myZipFilePath == null) {
            final String csvFileName = getBundledCsvFileName();
            return getClass().getClassLoader().getResourceAsStream(csvFileName);
        } else {
            return new FileInputStream(myZipFilePath);
        }
    }
}
