package com.blueconic.browscap.impl;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;

public class UserAgentServiceTest {

    @Test
    public void testUserAgentsFromExternalFile() throws IOException, ParseException {
        final int ITERATIONS = 10;



        final UserAgentService uas = new UserAgentService(".\\src\\main\\resources\\browscap-" + UserAgentService.BUNDLED_BROWSCAP_VERSION + ".zip");
        final UserAgentParser parser = uas.loadParser();

        int counter = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            counter += processUserAgentFile(parser);
        }
        System.out.print("Processed " + counter + " items");
    }

    @Test
    public void testUserAgentsFromFile() throws IOException, ParseException {
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
                // System.out.println(line);
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
                assertEquals(properties[y++], result.getDeviceType());

                x++;
            }
        }
        return x;
    }
}
