package com.blueconic.browscap.impl;

import static org.junit.Assert.assertEquals;

import java.io.*;
import java.util.Arrays;

import org.junit.Test;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;

public class UserAgentServiceTest {

    @Test
    public void testUserAgentsFromExternalFile() throws IOException, ParseException {
        final int ITERATIONS = 10;

        final UserAgentService uas = new UserAgentService(
                ".\\src\\main\\resources\\browscap-" + UserAgentService.BUNDLED_BROWSCAP_VERSION + ".zip");
        final UserAgentParser parser = uas.loadParser();

        int counter = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            counter += processUserAgentFile(parser);
        }
        System.out.print("Processed " + counter + " items");
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
        System.out.print("Processed " + counter + " items");
    }

    private int processUserAgentFile(final UserAgentParser parser) throws IOException, ParseException {
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
                System.out.println(result + "===" + properties[5]);
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

        final UserAgentService uas = new UserAgentService();
        final UserAgentParser parser = uas.loadParser(Arrays.asList("rendering_engine_maker", "rendering_engine_name",
                                                                    "platform_maker", "rendering_engine_version"));

        int counter = processCustomUserAgentFile(parser);
        System.out.print("Processed " + counter + " items");
    }

    private int processCustomUserAgentFile(final UserAgentParser parser) throws IOException, ParseException {
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
                System.out.println(result + "===" + properties[10]);
                System.out.println("Custom Fields" + result.getValues() + "===" + properties[10]);

                assertEquals(properties[y++], result.getBrowser());
                assertEquals(properties[y++], result.getBrowserType());
                assertEquals(properties[y++], result.getBrowserMajorVersion());
                assertEquals(properties[y++], result.getPlatform());
                assertEquals(properties[y++], result.getValues().get("platform_maker"));
                assertEquals(properties[y++], result.getPlatformVersion());
                assertEquals(properties[y++], result.getDeviceType());
                assertEquals(properties[y++], result.getValues().get("rendering_engine_name"));
                assertEquals(properties[y++], result.getValues().get("rendering_engine_maker"));
                assertEquals(properties[y], result.getValues().get("rendering_engine_version"));
                x++;
            }
        }
        return x;
    }
}
