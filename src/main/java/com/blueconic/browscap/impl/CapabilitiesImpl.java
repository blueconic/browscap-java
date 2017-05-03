package com.blueconic.browscap.impl;

import com.blueconic.browscap.Capabilities;

class CapabilitiesImpl implements Capabilities {
    public final static Capabilities DEFAULT = new CapabilitiesImpl("Default Browser", "Default Browser",
            UNKNOWN_BROWSCAP_VALUE,
            UNKNOWN_BROWSCAP_VALUE,
            UNKNOWN_BROWSCAP_VALUE,
            UNKNOWN_BROWSCAP_VALUE);

    private final String myBrowser;
    private final String myBrowserType;
    private final String myBrowserMajorVersion;
    private final String myDeviceType;
    private final String myPlatform;
    private final String myPlatformVersion;

    public CapabilitiesImpl(final String browser, final String browserType, final String browserMajorVersion,
            final String deviceType, final String platform, final String platformVersion) {

        myBrowser = browser;
        myBrowserType = browserType;
        myBrowserMajorVersion = browserMajorVersion;
        myDeviceType = deviceType;
        myPlatform = platform;
        myPlatformVersion = platformVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowser() {
        return myBrowser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserType() {
        return myBrowserType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserMajorVersion() {
        return myBrowserMajorVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatform() {
        return myPlatform;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatformVersion() {
        return myPlatformVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeviceType() {
        return myDeviceType;
    }

    @Override
    public String toString() {
        return "Capabilities{browser='" + myBrowser + "', browserType='" + myBrowserType + "', browserMajorVersion='"
                + myBrowserMajorVersion + "', deviceType='" + myDeviceType + "', platform='" + myPlatform
                + "', platformVersion='" + myPlatformVersion + '}';
    }
}
