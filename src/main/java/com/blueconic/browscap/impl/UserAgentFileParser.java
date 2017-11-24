package com.blueconic.browscap.impl;

import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
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
    public synchronized UserAgentParser parse(final Reader input, final List<String> fields) throws IOException, ParseException {

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

    private Rule getRule(final String[] record, final List<String> fields) throws ParseException {
        if (record.length <= 47) {
            return null;
        }

        // Normalize: lowercase and remove duplicate wildcards
        final String pattern = record[0].toLowerCase().replaceAll("\\*+", "*");
        try {

            HashMap<String, String> values = new HashMap<>();
            for (String field: fields) {
                switch (field.toLowerCase()) {
                    case "is_master_parent": values.put(field, getValue(record[1]));
                        break;
                    case "is_lite_mode": values.put(field, getValue(record[2]));
                        break;
                    case "parent": values.put(field, getValue(record[3]));
                        break;
                    case "comment": values.put(field, getValue(record[4]));
                        break;
                    case "browser": values.put(field, getValue(record[5]));
                        break;
                    case "browser_type": values.put(field, getValue(record[6]));
                        break;
                    case "browser_bits": values.put(field, getValue(record[7]));
                        break;
                    case "browser_maker": values.put(field, getValue(record[8]));
                        break;
                    case "browser_modus": values.put(field, getValue(record[9]));
                        break;
                    case "browser_version": values.put(field, getValue(record[10]));
                        break;
                    case "browser_major_version": values.put(field, getValue(record[11]));
                        break;
                    case "browser_minor_version": values.put(field, getValue(record[12]));
                        break;
                    case "platform": values.put(field, getValue(record[13]));
                        break;
                    case "platform_version": values.put(field, getValue(record[14]));
                        break;
                    case "platform_description": values.put(field, getValue(record[15]));
                        break;
                    case "platform_bits": values.put(field, getValue(record[16]));
                        break;
                    case "platform_maker": values.put(field, getValue(record[17]));
                        break;
                    case "is_alpha": values.put(field, getValue(record[18]));
                        break;
                    case "is_beta": values.put(field, getValue(record[19]));
                        break;
                    case "is_win16": values.put(field, getValue(record[20]));
                        break;
                    case "is_win32": values.put(field, getValue(record[21]));
                        break;
                    case "is_win64": values.put(field, getValue(record[22]));
                        break;
                    case "is_frames": values.put(field, getValue(record[23]));
                        break;
                    case "is_iframes": values.put(field, getValue(record[24]));
                        break;
                    case "is_tables": values.put(field, getValue(record[25]));
                        break;
                    case "is_cookies": values.put(field, getValue(record[26]));
                        break;
                    case "is_background_sounds": values.put(field, getValue(record[27]));
                        break;
                    case "is_javascript": values.put(field, getValue(record[28]));
                        break;
                    case "is_vbscript": values.put(field, getValue(record[29]));
                        break;
                    case "is_java_applets": values.put(field, getValue(record[30]));
                        break;
                    case "is_activex_controls": values.put(field, getValue(record[31]));
                        break;
                    case "is_mobile_device": values.put(field, getValue(record[32]));
                        break;
                    case "is_tablet": values.put(field, getValue(record[33]));
                        break;
                    case "is_syndication_reader": values.put(field, getValue(record[34]));
                        break;
                    case "is_crawler": values.put(field, getValue(record[35]));
                        break;
                    case "is_fake": values.put(field, getValue(record[36]));
                        break;
                    case "is_anonymized": values.put(field, getValue(record[37]));
                        break;
                    case "is_modified": values.put(field, getValue(record[38]));
                        break;
                    case "css_version": values.put(field, getValue(record[39]));
                        break;
                    case "aol_version": values.put(field, getValue(record[40]));
                        break;
                    case "device_name": values.put(field, getValue(record[41]));
                        break;
                    case "device_maker": values.put(field, getValue(record[42]));
                        break;
                    case "device_type": values.put(field, getValue(record[43]));
                        break;
                    case "device_pointing_method": values.put(field, getValue(record[44]));
                        break;
                    case "device_code_name": values.put(field, getValue(record[45]));
                        break;
                    case "device_brand_name": values.put(field, getValue(record[46]));
                        break;
                    case "rendering_engine_name": values.put(field, getValue(record[47]));
                        break;
                    case "rendering_engine_version": values.put(field, getValue(record[48]));
                        break;
                    case "rendering_engine_description": values.put(field, getValue(record[49]));
                        break;
                    case "rendering_engine_maker": values.put(field, getValue(record[50]));
                        break;
                }
            }

            // For backwards compatibility
            final String browser = getValue(record[5]);
            final String browserType = getValue(record[6]);
            final String browserMajorVersion = getValue(record[11]);
            final String deviceType = getValue(record[43]);
            final String platform = getValue(record[13]);
            final String platformVersion = getValue(record[14]);

            final Capabilities capabilities = new CapabilitiesImpl(browser, browserType, browserMajorVersion,
                    deviceType, platform, platformVersion, values);

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

    static String getValue(final String value) {
        if (StringUtil.isNotBlank(value)) {
            return value.trim().intern();
        }
        return Capabilities.UNKNOWN_BROWSCAP_VALUE;
    }

    Literal getLiteral(final String value) {
        return myUniqueLiterals.computeIfAbsent(value, Literal::new);
    }

    Rule createRule(final String pattern, final Capabilities capabilities, final List<String> fields) {

        final List<String> parts = getParts(pattern);
        if (parts.isEmpty()) {
            throw new IllegalStateException();
        }

        final String first = parts.get(0);
        if (parts.size() == 1) {
            if ("*".equals(first)) {
                // The default match all pattern
                HashMap<String, String> fieldValues = new HashMap<>();
                for (String field: fields) {
                    fieldValues.put(field, Capabilities.UNKNOWN_BROWSCAP_VALUE);
                }
                return new Rule(null, new Literal[0], null, "*", new CapabilitiesImpl("Default Browser", "Default Browser",
                        Capabilities.UNKNOWN_BROWSCAP_VALUE,
                        Capabilities.UNKNOWN_BROWSCAP_VALUE,
                        Capabilities.UNKNOWN_BROWSCAP_VALUE,
                        Capabilities.UNKNOWN_BROWSCAP_VALUE,
                        fieldValues));
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
