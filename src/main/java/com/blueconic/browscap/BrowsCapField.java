package com.blueconic.browscap;

public enum BrowsCapField {
    IS_MASTER_PARENT("is_master_parent"),
    IS_LITE_MODE("is_lite_mode"),
    PARENT("parent"),
    COMMENT("comment"),
    BROWSER("browser"),
    BROWSER_TYPE("browser_type"),
    BROWSER_BITS("browser_bits"),
    BROWSER_MAKER("browser_maker"),
    BROWSER_MODUS("browser_modus"),
    BROWSER_VERSION("browser_version"),
    BROWSER_MAJOR_VERSION("browser_major_version"),
    BROWSER_MINOR_VERSION("browser_minor_version"),
    PLATFORM("platform"),
    PLATFORM_VERSION("platform_version"),
    PLATFORM_DESCRIPTION("platform_description"),
    PLATFORM_BITS("platform_bits"),
    PLATFORM_MAKER("platform_maker"),
    IS_ALPHA("is_alpha"),
    IS_BETA("is_beta"),
    IS_WIN16("is_win16"),
    IS_WIN32("is_win32"),
    IS_WIN64("is_win64"),
    IS_IFRAMES("is_iframes"),
    IS_FRAMES("is_frames"),
    IS_TABLES("is_tables"),
    IS_COOKIES("is_cookies"),
    IS_BACKGROUND_SOUNDS("is_background_sounds"),
    IS_JAVASCRIPT("is_javascript"),
    IS_VBSCRIPT("is_vbscript"),
    IS_JAVA_APPLETS("is_java_applets"),
    IS_ACTIVEX_CONTROLS("is_activex_controls"),
    IS_MOBILE_DEVICES("is_mobile_device"),
    IS_TABLET("is_tablet"),
    IS_SYNDICATION_READER("is_syndication_reader"),
    IS_CRAWLER("is_crawler"),
    IS_FAKE("is_fake"),
    IS_ANONYMIZED("is_anonymized"),
    IS_MODIFIED("is_modified"),
    CSS_VERSION("css_version"),
    AOL_VERSION("aol_version"),
    DEVICE_NAME("device_name"),
    DEVICE_MAKER("device_maker"),
    DEVICE_TYPE("device_type"),
    DEVICE_POINTING_METHOD("device_pointing_method"),
    DEVICE_CODE_NAME("device_code_name"),
    DEVICE_BRAND_NAME("device_brand_name"),
    RENDERING_ENGINE_NAME("rendering_engine_name"),
    RENDERING_ENGINE_VERSION("rendering_engine_version"),
    RENDERING_ENGINE_DESCRIPTION("rendering_engine_description"),
    RENDERING_ENGINE_MAKER("rendering_engine_maker");

    private final String myField;

    /**
     * @param the browscap field identifier
     */
    private BrowsCapField(final String fieldIdentifier) {
        this.myField = fieldIdentifier;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return myField;
    }
}