package com.blueconic.browscap.domain;

public class Capabilities {
    public static final String UNKNOWN = "Unknown";

    public static final Capabilities DEFAULT =
            new Capabilities("Default Browser", "Default Browser", UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN);

    private final String myBrowser;
    private final String myBrowserType;
    private final String myBrowserMajorVersion;
    private final String myDeviceType;
    private final String myPlatform;
    private final String myPlatformVersion;

    public Capabilities(final String browser, final String browserType, final String browserMajorVersion,
            final String deviceType, final String platform, final String platformVersion) {

        myBrowser = browser;
        myBrowserType = browserType;
        myBrowserMajorVersion = browserMajorVersion;
        myDeviceType = deviceType;
        myPlatform = platform;
        myPlatformVersion = platformVersion;
    }

    public String getBrowser() {
        return myBrowser;
    }

    public String getBrowserType() {
        return myBrowserType;
    }

    public String getBrowserMajorVersion() {
        return myBrowserMajorVersion;
    }

    public String getPlatform() {
        return myPlatform;
    }

    public String getPlatformVersion() {
        return myPlatformVersion;
    }

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
