package com.blueconic.browscap.impl;

import static com.blueconic.browscap.Capabilities.UNKNOWN_BROWSCAP_VALUE;
import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.opencsv.CSVReader;

/**
 * This class is responsible for parsing rules and creating the efficient java representation.
 */
public class UserAgentFileParser {

    // Mapping substrings to unique literal for caching of lookups
    private final Map<String, Literal> myUniqueLiterals = new HashMap<>();

    private final Map<Capabilities, Capabilities> myCache = new HashMap<>();

    private final Map<String, String> myStrings = new HashMap<>();

    private final Mapper myMapper;

    private final Set<BrowsCapField> myFields;

    UserAgentFileParser(final Collection<BrowsCapField> fields) {
        myFields = new HashSet<>(fields);
        myMapper = new Mapper(myFields);
    }

    /**
     * Parses a csv stream of rules.
     * @param input The input stream
     * @param fields The fields that should be stored during parsing
     * @return a UserAgentParser based on the read rules
     * @throws IOException If reading the stream failed.
     * @throws ParseException
     */
    public static UserAgentParser parse(final Reader input, final Collection<BrowsCapField> fields)
            throws IOException, ParseException {
        return parse(input, fields, false);
    }

    /**
     * Parses a csv stream of rules.
     * @param input The input stream
     * @param fields The fields that should be stored during parsing
     * @param isLiteOnly Whether or not to use the lite-only matches.
     * @return a UserAgentParser based on the read rules
     * @throws IOException If reading the stream failed.
     * @throws ParseException
     */
    public static UserAgentParser parse(final Reader input, final Collection<BrowsCapField> fields, final boolean isLiteOnly) throws IOException, ParseException {
        // make sure we add the "lite" field for filtering
        if (isLiteOnly && !fields.contains(BrowsCapField.IS_LITE_MODE)) {
            fields.add(BrowsCapField.IS_LITE_MODE);
        }

        return new UserAgentFileParser(fields).parse(input, isLiteOnly);
    }

    private UserAgentParser parse(final Reader input, boolean isLiteOnly) throws IOException, ParseException {
        final List<Rule> rules = new ArrayList<>();
        try (final CSVReader csvReader = new CSVReader(input)) {
            final Iterator<String[]> iterator = csvReader.iterator();

            while (iterator.hasNext()) {
                final String[] record = iterator.next();

                final Rule rule = getRule(record);
                if (rule != null) {
                    if (!isLiteOnly || this.getBrowsCapFields(record).get(BrowsCapField.IS_LITE_MODE).equals("true")) {
                        rules.add(rule);
                    }
                }
            }
        }

        return new UserAgentParserImpl(rules.toArray(new Rule[0]), getDefaultCapabilities());
    }

    private UserAgentParser parse(final Reader input) throws IOException, ParseException {
        return parse(input, false);
    }

    Capabilities getDefaultCapabilities() {
        final Map<BrowsCapField, String> result = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : myFields) {
            result.put(field, UNKNOWN_BROWSCAP_VALUE);
        }
        return getCapabilities(result);
    }

    private Rule getRule(final String[] record) throws ParseException {
        if (record.length <= 47) {
            return null;
        }

        // Normalize: lowercase and remove duplicate wildcards
        final String pattern = normalizePattern(record[0]);
        try {
            final Map<BrowsCapField, String> values = getBrowsCapFields(record);
            final Capabilities capabilities = getCapabilities(values);
            final Rule rule = createRule(pattern, capabilities);

            // Check reconstructing the pattern
            if (!pattern.equals(rule.getPattern())) {
                throw new ParseException("Unable to parse " + pattern);
            }
            return rule;

        } catch (final IllegalStateException e) {
            throw new ParseException("Unable to parse " + pattern);
        }
    }

    private static String normalizePattern(final String pattern) {

        final String lowerCase = pattern.toLowerCase();
        if (lowerCase.contains("**")) {
            return lowerCase.replaceAll("\\*+", "*");
        }
        return lowerCase;
    }

    private Map<BrowsCapField, String> getBrowsCapFields(final String[] record) {
        final Map<BrowsCapField, String> values = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : myFields) {
            values.put(field, getValue(record[field.getIndex()]));
        }
        return values;
    }

    String getValue(final String value) {
        if (value == null) {
            return UNKNOWN_BROWSCAP_VALUE;
        }

        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return UNKNOWN_BROWSCAP_VALUE;
        }

        final String cached = myStrings.get(trimmed);
        if (cached != null) {
            return cached;
        }
        myStrings.put(trimmed, trimmed);
        return trimmed;
    }

    Capabilities getCapabilities(final Map<BrowsCapField, String> values) {

        final CapabilitiesImpl result = new CapabilitiesImpl(myMapper.getValues(values), myMapper);
        final Capabilities fromCache = myCache.get(result);
        if (fromCache != null) {
            return fromCache;
        }

        myCache.put(result, result);
        return result;
    }

    Literal getLiteral(final String value) {
        return myUniqueLiterals.computeIfAbsent(value, Literal::new);
    }

    Rule createRule(final String pattern, final Capabilities capabilities) {

        final List<String> parts = getParts(pattern);
        if (parts.isEmpty()) {
            throw new IllegalStateException();
        }

        final String first = parts.get(0);
        if (parts.size() == 1) {
            if ("*".equals(first)) {
                return getWildCardRule();
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

    private  Rule getWildCardRule() {
        // The default match all pattern
        final Map<BrowsCapField, String> fieldValues = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : myFields) {
            if (!field.isDefault()) {
                fieldValues.put(field, UNKNOWN_BROWSCAP_VALUE);
            }
        }
        final Capabilities capabilities = getCapabilities(fieldValues);
        return new Rule(null, new Literal[0], null, "*", capabilities);
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
