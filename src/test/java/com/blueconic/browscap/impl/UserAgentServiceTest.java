package com.blueconic.browscap.impl;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.BrowsCapField.BROWSER_MAJOR_VERSION;
import static com.blueconic.browscap.BrowsCapField.BROWSER_TYPE;
import static com.blueconic.browscap.BrowsCapField.DEVICE_TYPE;
import static com.blueconic.browscap.BrowsCapField.PLATFORM;
import static com.blueconic.browscap.BrowsCapField.PLATFORM_MAKER;
import static com.blueconic.browscap.BrowsCapField.PLATFORM_VERSION;
import static com.blueconic.browscap.BrowsCapField.RENDERING_ENGINE_MAKER;
import static com.blueconic.browscap.BrowsCapField.RENDERING_ENGINE_NAME;
import static com.blueconic.browscap.BrowsCapField.RENDERING_ENGINE_VERSION;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.Test;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;

public class UserAgentServiceTest {

    @Test
    public void testUserAgentsFromExternalFile() throws IOException, ParseException {
        final int ITERATIONS = 10;

        final Path path = Paths.get("src", "main", "resources", UserAgentService.getBundledCsvFileName());
        final UserAgentService uas = new UserAgentService(path.toString());

        final UserAgentParser parser = uas.loadParser();

        int counter = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            counter += processUserAgentFile(parser);
        }
        System.out.print("Processed " + counter + " items\n");
    }

    @Test
    public void testUserAgentsFromExternalInputStream() throws IOException, ParseException {
        final int ITERATIONS = 10;

        final Path path = Paths.get("src", "main", "resources", UserAgentService.getBundledCsvFileName());
        final InputStream fileStream = new FileInputStream(path.toString());
        final UserAgentService uas = new UserAgentService(fileStream);

        final UserAgentParser parser = uas.loadParser();

        int counter = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            counter += processUserAgentFile(parser);
        }
        System.out.print("Processed " + counter + " items\n");
    }

    @Test
    public void testUserAgentsFromBundledFile() throws IOException, ParseException {
        final int ITERATIONS = 10;

        final UserAgentService uas = new UserAgentService();
        final UserAgentParser parser = uas.loadParser();

        int counter = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            counter += processUserAgentFile(parser);
        }
        System.out.print("Processed " + counter + " items\n");
    }

    private int processUserAgentFile(final UserAgentParser parser) throws IOException {
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("useragents.txt");

        final BufferedReader in = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line = null;
        int x = 0;

        while ((line = in.readLine()) != null) {
            if (!"".equals(line)) {
                final String[] properties = line.split("    ");
                if (properties.length < 5) {
                    continue;
                }
                final Capabilities result = parser.parse(properties[5]); // check the values

                int y = 0;
                // System.out.println(result + "===" + properties[5]);

                assertEquals(properties[y++], result.getBrowser());
                assertEquals(properties[y++], result.getBrowserMajorVersion());
                assertEquals(properties[y++], result.getPlatform());
                assertEquals(properties[y++], result.getPlatformVersion());
                assertEquals(properties[y], result.getDeviceType());

                x++;
            }
        }
        return x;
    }

    @Test
    public void testUserAgentsFromBundledFileWithCustomFields() throws IOException, ParseException {

        final Collection<BrowsCapField> fields =
                asList(BROWSER, BROWSER_TYPE, BROWSER_MAJOR_VERSION, DEVICE_TYPE, PLATFORM,
                        PLATFORM_VERSION, RENDERING_ENGINE_VERSION, RENDERING_ENGINE_NAME, PLATFORM_MAKER,
                        RENDERING_ENGINE_MAKER);

        final UserAgentService uas = new UserAgentService();
        final UserAgentParser parser = uas.loadParser(fields);

        final int counter = processCustomUserAgentFile(parser);
        System.out.print("Processed " + counter + " items");
    }

    private int processCustomUserAgentFile(final UserAgentParser parser) throws IOException {
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("useragents_2.txt");
        final BufferedReader in = new BufferedReader(new InputStreamReader(resourceAsStream));
        String line = null;
        int x = 0;

        while ((line = in.readLine()) != null) {
            if (!"".equals(line)) {
                final String[] properties = line.split("    ");
                if (properties.length < 10) {
                    continue;
                }
                final Capabilities result = parser.parse(properties[10]); // check the values

                int y = 0;
                // System.out.println(result + "===" + properties[10] + "\n");
                // System.out.println("Custom Fields" + result.getValues() + "===" + properties[10] + "\n");

                assertEquals(properties[y++], result.getBrowser());
                assertEquals(properties[y++], result.getBrowserType());
                assertEquals(properties[y++], result.getBrowserMajorVersion());
                assertEquals(properties[y++], result.getPlatform());
                assertEquals(properties[y++], result.getValues().get(PLATFORM_MAKER));
                assertEquals(properties[y++], result.getPlatformVersion());
                assertEquals(properties[y++], result.getDeviceType());
                assertEquals(properties[y++], result.getValues().get(RENDERING_ENGINE_NAME));
                assertEquals(properties[y++], result.getValues().get(RENDERING_ENGINE_MAKER));
                assertEquals(properties[y], result.getValues().get(RENDERING_ENGINE_VERSION));
                x++;
            }
        }
        return x;
    }
}
