package com.blueconic.browscap.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;

class CapabilitiesImpl implements Capabilities {
    public final static Capabilities DEFAULT = new CapabilitiesImpl(Collections.emptyMap());

    private final Map<BrowsCapField, String> myValues = new HashMap<>();

    public CapabilitiesImpl(
            final Map<BrowsCapField, String> values) {
        // default values first, for backwards compatibility
        myValues.put(BrowsCapField.BROWSER, "Default Browser");
        myValues.put(BrowsCapField.BROWSER_TYPE, "Default Browser");
        myValues.put(BrowsCapField.BROWSER_MAJOR_VERSION, UNKNOWN_BROWSCAP_VALUE);
        myValues.put(BrowsCapField.DEVICE_TYPE, UNKNOWN_BROWSCAP_VALUE);
        myValues.put(BrowsCapField.PLATFORM, UNKNOWN_BROWSCAP_VALUE);
        myValues.put(BrowsCapField.PLATFORM_VERSION, UNKNOWN_BROWSCAP_VALUE);

        myValues.putAll(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowser() {
        return myValues.get(BrowsCapField.BROWSER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserType() {
        return myValues.get(BrowsCapField.BROWSER_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserMajorVersion() {
        return myValues.get(BrowsCapField.BROWSER_MAJOR_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatform() {
        return myValues.get(BrowsCapField.PLATFORM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatformVersion() {
        return myValues.get(BrowsCapField.PLATFORM_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeviceType() {
        return myValues.get(BrowsCapField.DEVICE_TYPE);
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
