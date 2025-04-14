package com.blueconic.browscap.impl;

import static java.util.Arrays.parallelSort;
import static java.util.Arrays.sort;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.UserAgentParser;

/**
 * This class is responsible for determining the best matching useragent rule to determine the properties for a
 * useragent string.
 */
class UserAgentParserImpl implements UserAgentParser {

    // Common substrings to filter irrelevant rules and speed up processing
    static final String[] COMMON = {"-", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "profile", "player",
            "compatible", "android", "google", "tab", "transformer", "lenovo", "micro", "edge", "safari", "opera",
            "chrome", "firefox", "msie", "chromium", "cpu os ", "cpu iphone os ", "windows nt ", "mac os x ", "linux",
            "bsd", "windows phone", "iphone", "pad", "blackberry", "nokia", "alcatel", "ucbrowser", "mobile", "ie",
            "mercury", "samsung", "browser", "wow64", "silk", "lunascape", "crios", "epiphany", "konqueror", "version",
            "rv:", "build", "bot", "like gecko", "applewebkit", "trident", "mozilla", "windows nt 4", "windows nt 5.0",
            "windows nt 5.1", "windows nt 5.2", "windows nt 6.0", "windows nt 6.1", "windows nt 6.2", "windows nt 6.3",
            "windows nt 10.0", "android?4.0", "android?4.1", "android?4.2", "android?4.3", "android?4.4", "android?2.3",
            "android?5"};

    // Common prefixes to filter irrelevant rules and speed up processing
    static final String[] FILTER_PREFIXES = {"mozilla/5.0", "mozilla/4"};

    // All useragent rule ordered by size and alphabetically
    private final Rule[] myRules;

    // Filters for filtering irrelevant rules and speed up processing
    private final List<Filter> myFilters;

    // The default Capabilities
    private final Capabilities myDefaultCapabilities;

    // The domain of literals for this parser
    private final LiteralDomain myDomain;

    /**
     * Creates a new parser based on a collection of rules.
     * @param rules The rules, ordered by priority
     * @param domain The domain of literals
     * @param defaultCapabilities The default capabilities
     */
    UserAgentParserImpl(final Rule[] rules, final LiteralDomain domain, final Capabilities defaultCapabilities) {
        myDomain = domain;
        myRules = getOrderedRules(rules);
        myFilters = buildFilters();
        myDefaultCapabilities = defaultCapabilities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Capabilities parse(final String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return myDefaultCapabilities;
        }

        final SearchableString searchString = myDomain.getSearchableString(userAgent.toLowerCase());

        final BitSet includes = getIncludeRules(searchString, myFilters);

        for (int i = includes.nextSetBit(0); i >= 0; i = includes.nextSetBit(i + 1)) {
            final Rule rule = myRules[i];
            if (rule.matches(searchString)) {
                return rule.getCapabilities();
            }
        }

        return myDefaultCapabilities;
    }

    BitSet getIncludeRules(final SearchableString searchString, final List<Filter> filters) {
        final BitSet excludes = new BitSet(myRules.length);
        for (final Filter filter : filters) {
            filter.applyExcludes(searchString, excludes);
        }

        // Convert flip the excludes to determine the includes
        excludes.flip(0, myRules.length);
        return excludes;
    }

    // Sort by size and alphabet, so the first match can be returned immediately
    static Rule[] getOrderedRules(final Rule[] rules) {

        final Comparator<Rule> c = Comparator.comparing(Rule::getSize).reversed().thenComparing(Rule::getPattern);

        final Rule[] result = Arrays.copyOf(rules, rules.length);
        sort(result, c);
        return result;
    }

    List<Filter> buildFilters() {
        final List<Filter> result = new ArrayList<>();

        // Build filters for specific prefix constraints
        for (final String pattern : FILTER_PREFIXES) {
            result.add(createPrefixFilter(pattern));
        }

        // Build filters for specific contains constraints
        Stream.of(COMMON).map(this::createContainsFilter).forEach(result::add);

        return result;
    }

    Filter createContainsFilter(final String pattern) {
        final Literal literal = myDomain.createLiteral(pattern);

        final Predicate<SearchableString> pred = c -> c.getIndices(literal).length > 0;

        final Predicate<Rule> matches = rule -> rule.requires(pattern);

        return new Filter(pred, matches);
    }

    Filter createPrefixFilter(final String pattern) {
        final Literal literal = myDomain.createLiteral(pattern);

        final Predicate<SearchableString> pred = s -> s.startsWith(literal);

        final Predicate<Rule> matches = rule -> {
            final Literal prefix = rule.getPrefix();
            return prefix != null && prefix.toString().startsWith(pattern);
        };

        return new Filter(pred, matches);
    }

    /**
     * Filter expression to can exclude a number of rules if a useragent doesn't meet it's predicate.
     */
    class Filter {

        private final Predicate<SearchableString> myUserAgentPredicate;
        private final BitSet myMask;

        /**
         * Creates a filter.
         * @param userAgentPredicate The predicate for matching user agents.
         * @param patternPredicate The corresponding predicate for matching rule
         */
        Filter(final Predicate<SearchableString> userAgentPredicate, final Predicate<Rule> patternPredicate) {
            myUserAgentPredicate = userAgentPredicate;
            myMask = new BitSet(myRules.length);
            for (int i = 0; i < myRules.length; i++) {
                if (patternPredicate.test(myRules[i])) {
                    myMask.set(i);
                }
            }
        }

        void applyExcludes(final SearchableString userAgent, final BitSet resultExcludes) {
            if (!myUserAgentPredicate.test(userAgent)) {
                resultExcludes.or(myMask);
            }
        }
    }
}
