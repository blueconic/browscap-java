package com.blueconic.browscap.impl;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.BrowsCapField.BROWSER_MAJOR_VERSION;
import static com.blueconic.browscap.BrowsCapField.BROWSER_TYPE;
import static com.blueconic.browscap.BrowsCapField.DEVICE_TYPE;
import static com.blueconic.browscap.BrowsCapField.PLATFORM;
import static com.blueconic.browscap.BrowsCapField.PLATFORM_VERSION;
import static java.util.Collections.emptyMap;

import java.util.EnumMap;
import java.util.Map;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;

class CapabilitiesImpl implements Capabilities {

    public static final Capabilities DEFAULT = new CapabilitiesImpl(emptyMap());

    private final Map<BrowsCapField, String> myValues;

    public CapabilitiesImpl(final Map<BrowsCapField, String> values) {

        // default values first, for backwards compatibility
        myValues = new EnumMap<>(BrowsCapField.class);
        myValues.put(BROWSER, "Default Browser");
        myValues.put(BROWSER_TYPE, "Default Browser");
        myValues.put(BROWSER_MAJOR_VERSION, UNKNOWN_BROWSCAP_VALUE);
        myValues.put(DEVICE_TYPE, UNKNOWN_BROWSCAP_VALUE);
        myValues.put(PLATFORM, UNKNOWN_BROWSCAP_VALUE);
        myValues.put(PLATFORM_VERSION, UNKNOWN_BROWSCAP_VALUE);

        myValues.putAll(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowser() {
        return myValues.get(BROWSER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserType() {
        return myValues.get(BROWSER_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserMajorVersion() {
        return myValues.get(BROWSER_MAJOR_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatform() {
        return myValues.get(PLATFORM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatformVersion() {
        return myValues.get(PLATFORM_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeviceType() {
        return myValues.get(DEVICE_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<BrowsCapField, String> getValues() {
        return myValues;
    }

    @Override
    public String toString() {
        return "CapabilitiesImpl [myValues=" + myValues + "]";
    }
}