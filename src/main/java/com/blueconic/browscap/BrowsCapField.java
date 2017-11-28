package com.blueconic.browscap;

public enum BrowsCapField {

    IS_MASTER_PARENT,
    IS_LITE_MODE,
    PARENT,
    COMMENT,
    BROWSER(true),
    BROWSER_TYPE(true),
    BROWSER_BITS,
    BROWSER_MAKER,
    BROWSER_MODUS,
    BROWSER_VERSION,
    BROWSER_MAJOR_VERSION(true),
    BROWSER_MINOR_VERSION,
    PLATFORM(true),
    PLATFORM_VERSION(true),
    PLATFORM_DESCRIPTION,
    PLATFORM_BITS,
    PLATFORM_MAKER,
    IS_ALPHA,
    IS_BETA,
    IS_WIN16,
    IS_WIN32,
    IS_WIN64,
    IS_IFRAMES,
    IS_FRAMES,
    IS_TABLES,
    IS_COOKIES,
    IS_BACKGROUND_SOUNDS,
    IS_JAVASCRIPT,
    IS_VBSCRIPT,
    IS_JAVA_APPLETS,
    IS_ACTIVEX_CONTROLS,
    IS_MOBILE_DEVICE,
    IS_TABLET,
    IS_SYNDICATION_READER,
    IS_CRAWLER,
    IS_FAKE,
    IS_ANONYMIZED,
    IS_MODIFIED,
    CSS_VERSION,
    AOL_VERSION,
    DEVICE_NAME,
    DEVICE_MAKER,
    DEVICE_TYPE(true),
    DEVICE_POINTING_METHOD,
    DEVICE_CODE_NAME,
    DEVICE_BRAND_NAME,
    RENDERING_ENGINE_NAME,
    RENDERING_ENGINE_VERSION,
    RENDERING_ENGINE_DESCRIPTION,
    RENDERING_ENGINE_MAKER;

    private final boolean myIsDefault;

    BrowsCapField() {
        myIsDefault = false;
    }

    BrowsCapField(final boolean isDefault) {
        myIsDefault = isDefault;
    }

    public int getIndex() {
        return ordinal() + 1;
    }

    public boolean isDefault() {
        return myIsDefault;
    }
}