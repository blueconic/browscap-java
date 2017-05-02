package com.blueconic.browscap;

import static com.blueconic.browscap.domain.Capabilities.UNKNOWN;
import static java.util.Collections.singleton;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.blueconic.browscap.domain.Capabilities;
import com.blueconic.browscap.exception.ParseException;
import com.blueconic.browscap.util.StringUtil;
import com.opencsv.CSVReader;

/**
 * This class is responsible for parsing rules and creating the efficient java representation.
 */
class UserAgentFileParser {

    // The default match all pattern
    private static final Rule WILDCARD = new Rule(null, new Literal[0], null, "*", Capabilities.DEFAULT);

    // Mapping substrings to unique literal for caching of lookups
    private final Map<String, Literal> myUniqueLiterals = new TreeMap<>();

    /**
     * Parses a csv stream of rules.
     * @param input The input stream
     * @param logService The log service.
     * @return a UserAgentParser based on the read rules
     * @throws IOException If reading the stream failed.
     * @throws ParseException
     */
    synchronized UserAgentParser parse(final Reader input) throws IOException, ParseException {

        final List<Rule> rules = new ArrayList<>();
        try (final CSVReader csvReader = new CSVReader(input)) {
            final Iterator<String[]> iterator = csvReader.iterator();

            while (iterator.hasNext()) {
                final String[] record = iterator.next();
                final Rule rule = getRule(record);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }

        return new UserAgentParser(rules.toArray(new Rule[0]));
    }

    private Rule getRule(final String[] record) throws ParseException {
        if (record.length <= 47) {
            return null;
        }

        // Normalize: lowercase and remove duplicate wildcards
        final String pattern = record[0].toLowerCase().replaceAll("\\*+", "*");
        try {

            final String browser = getValue(record[5]);
            final String browserType = getValue(record[6]);
            final String browserMajorVersion = getValue(record[11]);
            final String deviceType = getValue(record[43]);
            final String platform = getValue(record[13]);
            final String platformVersion = getValue(record[14]);
            final Capabilities capabilities =
                    new Capabilities(browser, browserType, browserMajorVersion, deviceType, platform, platformVersion);

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

    static String getValue(final String value) {
        if (StringUtil.isNotBlank(value)) {
            return value.trim().intern();
        }
        return UNKNOWN;
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
                return WILDCARD;
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