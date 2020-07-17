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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;

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

    private final LiteralDomain myDomain = new LiteralDomain();

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
        return new UserAgentFileParser(fields).parse(input);
    }

    private UserAgentParser parse(final Reader input) throws ParseException {
        final List<Rule> rules = new ArrayList<>();

        final CsvParserSettings settings = new CsvParserSettings();
        final CsvParser csvParser = new CsvParser(settings);
        csvParser.beginParsing(input);
        Record record;
        while ((record = csvParser.parseNextRecord()) != null) {
            final Rule rule = getRule(record);
            if (rule != null) {
                rules.add(rule);
            }
        }
        return new UserAgentParserImpl(rules.toArray(new Rule[0]), myDomain, getDefaultCapabilities());
    }

    Capabilities getDefaultCapabilities() {
        final Map<BrowsCapField, String> result = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : myFields) {
            result.put(field, UNKNOWN_BROWSCAP_VALUE);
        }
        return getCapabilities(result);
    }

    private Rule getRule(final Record record) throws ParseException {
        if (record.getValues().length <= 47) {
            return null;
        }

        // Normalize: lowercase and remove duplicate wildcards
        final String pattern = normalizePattern(record.getString(0));
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

    private Map<BrowsCapField, String> getBrowsCapFields(final Record record) {
        final Map<BrowsCapField, String> values = new EnumMap<>(BrowsCapField.class);
        for (final BrowsCapField field : myFields) {
            values.put(field, getValue(record.getString(field.getIndex())));
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
        return myUniqueLiterals.computeIfAbsent(value, myDomain::createLiteral);
    }

    LiteralDomain getDomain() {
        return myDomain;
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

    private Rule getWildCardRule() {
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
