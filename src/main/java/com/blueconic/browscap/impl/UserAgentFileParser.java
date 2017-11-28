package com.blueconic.browscap.impl;

import static com.blueconic.browscap.Capabilities.UNKNOWN_BROWSCAP_VALUE;
import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    private final Map<String, Literal> myUniqueLiterals = new TreeMap<>();

    /**
     * Parses a csv stream of rules.
     * @param input The input stream
     * @return a UserAgentParser based on the read rules
     * @throws IOException If reading the stream failed.
     * @throws ParseException
     */
    public synchronized UserAgentParser parse(final Reader input, final Collection<BrowsCapField> fields)
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

    private Rule getRule(final String[] record, final Collection<BrowsCapField> fields) throws ParseException {
        if (record.length <= 47) {
            return null;
        }

        // Normalize: lowercase and remove duplicate wildcards
        final String pattern = record[0].toLowerCase().replaceAll("\\*+", "*");
        try {
            final Map<BrowsCapField, String> values = getBrowsCapFields(record, fields);
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

    private static Map<BrowsCapField, String> getBrowsCapFields(final String[] record,
            final Collection<BrowsCapField> fields) {
        final Map<BrowsCapField, String> values = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : fields) {
            values.put(field, getValue(record[field.getIndex()]));
        }
        return values;
    }

    static String getValue(final String value) {
        if (value == null) {
            return UNKNOWN_BROWSCAP_VALUE;
        }

        final String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return UNKNOWN_BROWSCAP_VALUE;
        }
        return trimmed.intern();
    }

    Literal getLiteral(final String value) {
        return myUniqueLiterals.computeIfAbsent(value, Literal::new);
    }

    Rule createRule(final String pattern, final Capabilities capabilities, final Collection<BrowsCapField> fields) {

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

    private static Rule getWildCardRule(final Collection<BrowsCapField> fields) {
        // The default match all pattern
        final Map<BrowsCapField, String> fieldValues = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : fields) {
            if (!field.isDefault()) {
                fieldValues.put(field, UNKNOWN_BROWSCAP_VALUE);
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
