package com.blueconic.browscap.impl;

import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import com.opencsv.CSVReader;

/**
 * This class is responsible for parsing rules and creating the efficient java representation.
 */
public class UserAgentFileParser {
    // Mapping substrings to unique literal for caching of lookups
    private final Map<String, Literal> myUniqueLiterals = new TreeMap<>();

    /**
     * Parses a csv stream of rules.
     * @param input The input stream
     * @return a UserAgentParser based on the read rules
     * @throws IOException If reading the stream failed.
     * @throws ParseException
     */
    public synchronized UserAgentParser parse(final Reader input, final List<BrowsCapField> fields)
            throws IOException, ParseException {

        final List<Rule> rules = new ArrayList<>();
        try (final CSVReader csvReader = new CSVReader(input)) {
            final Iterator<String[]> iterator = csvReader.iterator();

            while (iterator.hasNext()) {
                final String[] record = iterator.next();
                final Rule rule = getRule(record, fields);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }

        return new UserAgentParserImpl(rules.toArray(new Rule[0]), fields);
    }

    private Rule getRule(final String[] record, final List<BrowsCapField> fields) throws ParseException {
        if (record.length <= 47) {
            return null;
        }

        // Normalize: lowercase and remove duplicate wildcards
        final String pattern = record[0].toLowerCase().replaceAll("\\*+", "*");
        try {
            final HashMap<BrowsCapField, String> values = getBrowsCapFields(record, fields);
            final Capabilities capabilities = new CapabilitiesImpl(values);
            final Rule rule = createRule(pattern, capabilities, fields);

            // Check reconstructing the pattern
            if (!pattern.equals(rule.getPattern())) {
                throw new ParseException("Unable to parse " + pattern);
            }
            return rule;

        } catch (final IllegalStateException e) {
            throw new ParseException("Unable to parse " + pattern);
        }
    }

    private HashMap<BrowsCapField, String> getBrowsCapFields(final String[] record, final List<BrowsCapField> fields) {
        final HashMap<BrowsCapField, String> values = new HashMap<>();
        for (final BrowsCapField field : fields) {
            switch (field.toString()) {
            case "is_master_parent":
                values.put(BrowsCapField.IS_MASTER_PARENT, getValue(record[1]));
                break;
            case "is_lite_mode":
                values.put(BrowsCapField.IS_LITE_MODE, getValue(record[2]));
                break;
            case "parent":
                values.put(BrowsCapField.PARENT, getValue(record[3]));
                break;
            case "comment":
                values.put(BrowsCapField.COMMENT, getValue(record[4]));
                break;
            case "browser":
                values.put(BrowsCapField.BROWSER, getValue(record[5]));
                break;
            case "browser_type":
                values.put(BrowsCapField.BROWSER_TYPE, getValue(record[6]));
                break;
            case "browser_bits":
                values.put(BrowsCapField.BROWSER_BITS, getValue(record[7]));
                break;
            case "browser_maker":
                values.put(BrowsCapField.BROWSER_MAKER, getValue(record[8]));
                break;
            case "browser_modus":
                values.put(BrowsCapField.BROWSER_MODUS, getValue(record[9]));
                break;
            case "browser_version":
                values.put(BrowsCapField.BROWSER_VERSION, getValue(record[10]));
                break;
            case "browser_major_version":
                values.put(BrowsCapField.BROWSER_MAJOR_VERSION, getValue(record[11]));
                break;
            case "browser_minor_version":
                values.put(BrowsCapField.BROWSER_MINOR_VERSION, getValue(record[12]));
                break;
            case "platform":
                values.put(BrowsCapField.PLATFORM, getValue(record[13]));
                break;
            case "platform_version":
                values.put(BrowsCapField.PLATFORM_VERSION, getValue(record[14]));
                break;
            case "platform_description":
                values.put(BrowsCapField.PLATFORM_DESCRIPTION, getValue(record[15]));
                break;
            case "platform_bits":
                values.put(BrowsCapField.PLATFORM_BITS, getValue(record[16]));
                break;
            case "platform_maker":
                values.put(BrowsCapField.PLATFORM_MAKER, getValue(record[17]));
                break;
            case "is_alpha":
                values.put(BrowsCapField.IS_ALPHA, getValue(record[18]));
                break;
            case "is_beta":
                values.put(BrowsCapField.IS_BETA, getValue(record[19]));
                break;
            case "is_win16":
                values.put(BrowsCapField.IS_WIN16, getValue(record[20]));
                break;
            case "is_win32":
                values.put(BrowsCapField.IS_WIN32, getValue(record[21]));
                break;
            case "is_win64":
                values.put(BrowsCapField.IS_WIN64, getValue(record[22]));
                break;
            case "is_frames":
                values.put(BrowsCapField.IS_FRAMES, getValue(record[23]));
                break;
            case "is_iframes":
                values.put(BrowsCapField.IS_IFRAMES, getValue(record[24]));
                break;
            case "is_tables":
                values.put(BrowsCapField.IS_TABLES, getValue(record[25]));
                break;
            case "is_cookies":
                values.put(BrowsCapField.IS_COOKIES, getValue(record[26]));
                break;
            case "is_background_sounds":
                values.put(BrowsCapField.IS_BACKGROUND_SOUNDS, getValue(record[27]));
                break;
            case "is_javascript":
                values.put(BrowsCapField.IS_JAVASCRIPT, getValue(record[28]));
                break;
            case "is_vbscript":
                values.put(BrowsCapField.IS_VBSCRIPT, getValue(record[29]));
                break;
            case "is_java_applets":
                values.put(BrowsCapField.IS_JAVA_APPLETS, getValue(record[30]));
                break;
            case "is_activex_controls":
                values.put(BrowsCapField.IS_ACTIVEX_CONTROLS, getValue(record[31]));
                break;
            case "is_mobile_device":
                values.put(BrowsCapField.IS_MOBILE_DEVICES, getValue(record[32]));
                break;
            case "is_tablet":
                values.put(BrowsCapField.IS_TABLET, getValue(record[33]));
                break;
            case "is_syndication_reader":
                values.put(BrowsCapField.IS_SYNDICATION_READER, getValue(record[34]));
                break;
            case "is_crawler":
                values.put(BrowsCapField.IS_CRAWLER, getValue(record[35]));
                break;
            case "is_fake":
                values.put(BrowsCapField.IS_FAKE, getValue(record[36]));
                break;
            case "is_anonymized":
                values.put(BrowsCapField.IS_ANONYMIZED, getValue(record[37]));
                break;
            case "is_modified":
                values.put(BrowsCapField.IS_MODIFIED, getValue(record[38]));
                break;
            case "css_version":
                values.put(BrowsCapField.CSS_VERSION, getValue(record[39]));
                break;
            case "aol_version":
                values.put(BrowsCapField.AOL_VERSION, getValue(record[40]));
                break;
            case "device_name":
                values.put(BrowsCapField.DEVICE_NAME, getValue(record[41]));
                break;
            case "device_maker":
                values.put(BrowsCapField.DEVICE_MAKER, getValue(record[42]));
                break;
            case "device_type":
                values.put(BrowsCapField.DEVICE_TYPE, getValue(record[43]));
                break;
            case "device_pointing_method":
                values.put(BrowsCapField.DEVICE_POINTING_METHOD, getValue(record[44]));
                break;
            case "device_code_name":
                values.put(BrowsCapField.DEVICE_CODE_NAME, getValue(record[45]));
                break;
            case "device_brand_name":
                values.put(BrowsCapField.DEVICE_BRAND_NAME, getValue(record[46]));
                break;
            case "rendering_engine_name":
                values.put(BrowsCapField.RENDERING_ENGINE_NAME, getValue(record[47]));
                break;
            case "rendering_engine_version":
                values.put(BrowsCapField.RENDERING_ENGINE_VERSION, getValue(record[48]));
                break;
            case "rendering_engine_description":
                values.put(BrowsCapField.RENDERING_ENGINE_DESCRIPTION, getValue(record[49]));
                break;
            case "rendering_engine_maker":
                values.put(BrowsCapField.RENDERING_ENGINE_MAKER, getValue(record[50]));
                break;
            }
        }
        return values;
    }

    static String getValue(final String value) {
        if (StringUtil.isNotBlank(value)) {
            return value.trim().intern();
        }
        return Capabilities.UNKNOWN_BROWSCAP_VALUE;
    }

    Literal getLiteral(final String value) {
        return myUniqueLiterals.computeIfAbsent(value, Literal::new);
    }

    Rule createRule(final String pattern, final Capabilities capabilities, final List<BrowsCapField> fields) {

        final List<String> parts = getParts(pattern);
        if (parts.isEmpty()) {
            throw new IllegalStateException();
        }

        final String first = parts.get(0);
        if (parts.size() == 1) {
            if ("*".equals(first)) {
                return getWildCardRule(fields);
            }
            return new Rule(getLiteral(first), null, null, pattern, capabilities);
        }

        final LinkedList<String> suffixes = new LinkedList<>(parts);

        Literal prefix = null;
        if (!"*".equals(first)) {
            prefix = getLiteral(first);
            suffixes.remove(0);
        }

        final String last = parts.get(parts.size() - 1);
        Literal postfix = null;
        if (!"*".equals(last)) {
            postfix = getLiteral(last);
            suffixes.removeLast();
        }
        suffixes.removeAll(singleton("*"));
        final Literal[] suffixArray = new Literal[suffixes.size()];
        for (int i = 0; i < suffixArray.length; i++) {
            suffixArray[i] = getLiteral(suffixes.get(i));
        }

        return new Rule(prefix, suffixArray, postfix, pattern, capabilities);
    }

    private Rule getWildCardRule(final List<BrowsCapField> fields) {
        // The default match all pattern
        final HashMap<BrowsCapField, String> fieldValues = new HashMap<>();
        for (final BrowsCapField field : fields) {
            if (!UserAgentService.DEFAULT_FIELDS.contains(field)) {
                fieldValues.put(field, Capabilities.UNKNOWN_BROWSCAP_VALUE);
            }
        }
        return new Rule(null, new Literal[0], null, "*", new CapabilitiesImpl(fieldValues));
    }

    static List<String> getParts(final String pattern) {

        final List<String> parts = new ArrayList<>();

        final StringBuilder literal = new StringBuilder();
        for (final char c : pattern.toCharArray()) {
            if (c == '*') {
                if (literal.length() != 0) {
                    parts.add(literal.toString());
                    literal.setLength(0);
                }
                parts.add(String.valueOf(c));

            } else {
                literal.append(c);
            }
        }
        if (literal.length() != 0) {
            parts.add(literal.toString());
            literal.setLength(0);
        }

        return parts;
    }
}
