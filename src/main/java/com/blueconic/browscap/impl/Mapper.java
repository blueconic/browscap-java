package com.blueconic.browscap.impl;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.BrowsCapField.BROWSER_MAJOR_VERSION;
import static com.blueconic.browscap.BrowsCapField.BROWSER_TYPE;
import static com.blueconic.browscap.BrowsCapField.DEVICE_TYPE;
import static com.blueconic.browscap.BrowsCapField.PLATFORM;
import static com.blueconic.browscap.BrowsCapField.PLATFORM_VERSION;
import static com.blueconic.browscap.Capabilities.UNKNOWN_BROWSCAP_VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.blueconic.browscap.BrowsCapField;

class Mapper {

    private final Map<BrowsCapField, Integer> myIndices;

    Mapper(final Collection<BrowsCapField> fields) {
        // Get all fields
        final Set<BrowsCapField> all = new HashSet<>(fields);
        for (final BrowsCapField field : BrowsCapField.values()) {
            if (field.isDefault()) {
                all.add(field);
            }
        }

        // Get all unique values and keep a fixed order
        myIndices = new EnumMap<>(BrowsCapField.class);
        final List<BrowsCapField> ordered = new ArrayList<>(all);
        for (int i = 0; i < ordered.size(); i++) {
            myIndices.put(ordered.get(i), i);
        }
    }

    String[] getValues(final Map<BrowsCapField, String> values) {
        final String[] result = new String[myIndices.size()];

        // default values first, for backwards compatibility
        put(result, BROWSER, "Default Browser");
        put(result, BROWSER_TYPE, "Default Browser");
        put(result, BROWSER_MAJOR_VERSION, UNKNOWN_BROWSCAP_VALUE);
        put(result, DEVICE_TYPE, UNKNOWN_BROWSCAP_VALUE);
        put(result, PLATFORM, UNKNOWN_BROWSCAP_VALUE);
        put(result, PLATFORM_VERSION, UNKNOWN_BROWSCAP_VALUE);

        for (final Entry<BrowsCapField, String> entry : values.entrySet()) {
            put(result, entry.getKey(), entry.getValue());
        }
        return result;
    }

    public Map<BrowsCapField, String> getAll(final String[] values) {
        final Map<BrowsCapField, String> result = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : myIndices.keySet()) {
            result.put(field, getValue(values, field));
        }
        return result;
    }

    String getValue(final String[] values, final BrowsCapField field) {
        final Integer index = myIndices.get(field);
        if (index != null) {
            return values[index];
        }
        return null;
    }

    private void put(final String[] values, final BrowsCapField field, final String value) {
        final Integer index = myIndices.get(field);
        if (index != null) {
            values[index] = value;
        }
    }
}