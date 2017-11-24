package com.blueconic.browscap;

import java.util.Map;

public interface Capabilities {
    String UNKNOWN_BROWSCAP_VALUE = "Unknown";

    /**
     * Returns the browser value (e.g. Chrome)
     * @return the browser
     */
    String getBrowser();

    /**
     * Returns the browser type (e.g. Browser or Application)
     * @return the browser type
     */
    String getBrowserType();

    /**
     * Returns the major version of the browser (e.g. 55 in case of Chrome)
     * @return the browser major version
     */
    String getBrowserMajorVersion();

    /**
     * Returns the platform name (e.g. Android, iOS, Win7, Win10)
     * @return the platform
     */
    String getPlatform();

    /**
     * Returns the platform version (e.g. 4.2, 10 depending on what the platform is)
     * @return the platform version
     */
    String getPlatformVersion();

    /**
     * Returns the device type (e.g. Mobile Phone, Desktop, Tablet, Console, TV Device)
     * @return the device type
     */
    String getDeviceType();

    /**
     * Returns the Map of values with the fields passed to the parser while loading
     * @return the map of values
     */
    Map<BrowsCapField, String> getValues();

}
