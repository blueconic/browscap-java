package com.blueconic.browscap.impl;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.BrowsCapField.BROWSER_MAJOR_VERSION;
import static com.blueconic.browscap.BrowsCapField.BROWSER_TYPE;
import static com.blueconic.browscap.BrowsCapField.DEVICE_TYPE;
import static com.blueconic.browscap.BrowsCapField.PLATFORM;
import static com.blueconic.browscap.BrowsCapField.PLATFORM_VERSION;

import java.util.Arrays;
import java.util.Map;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;

class CapabilitiesImpl implements Capabilities {

    private final String[] myValues;
    private final Mapper myMapper;

    CapabilitiesImpl(final String[] values, final Mapper mapper) {
        myValues = values;
        myMapper = mapper;
    }

    @Override
    public String getValue(final BrowsCapField field) {
        return myMapper.getValue(myValues, field);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowser() {
        return getValue(BROWSER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserType() {
        return getValue(BROWSER_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBrowserMajorVersion() {
        return getValue(BROWSER_MAJOR_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatform() {
        return getValue(PLATFORM);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPlatformVersion() {
        return getValue(PLATFORM_VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDeviceType() {
        return getValue(DEVICE_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<BrowsCapField, String> getValues() {
        return myMapper.getAll(myValues);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(myValues);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CapabilitiesImpl)) {
            return false;
        }

        final CapabilitiesImpl other = (CapabilitiesImpl) obj;
        if (myMapper != other.myMapper) {
            return false;
        }
        
        return Arrays.equals(myValues, other.myValues);
    }

    @Override
    public String toString() {
        return "CapabilitiesImpl [myValues=" + getValues() + "]";
    }
}