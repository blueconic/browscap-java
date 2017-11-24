/**
 * $LastChangedBy$
 * $LastChangedDate$
 * $LastChangedRevision$
 * $HeadURL$
 *
 * Copyright 2014 BlueConic Inc./BlueConic B.V. All rights reserved.
 */
package com.blueconic.browscap.impl;

import static com.blueconic.browscap.impl.CapabilitiesImpl.DEFAULT;
import static com.blueconic.browscap.impl.UserAgentParserImpl.getOrderedRules;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;
import java.util.Collections;

import org.junit.Test;

import com.blueconic.browscap.impl.UserAgentParserImpl.Filter;

public class UserAgentParserTest {

    @Test
    public void testExcludes() {

        final int length = 1017;
        final BitSet excludes = new BitSet(length);

        final BitSet excludeFilter1 = new BitSet(length);
        excludeFilter1.set(10);
        excludeFilter1.set(20);

        final BitSet excludeFilter2 = new BitSet(length);
        excludeFilter1.set(20);
        excludeFilter1.set(30);

        excludes.or(excludeFilter1);
        excludes.or(excludeFilter2);
        assertEquals(3, excludes.cardinality());

        excludes.flip(0, length);
        assertEquals(length - 3, excludes.cardinality());

        assertTrue(excludes.get(0));
        assertTrue(excludes.get(length - 1));
        assertFalse(excludes.get(10));
        assertFalse(excludes.get(20));
        assertFalse(excludes.get(30));

        assertEquals(length - 1, excludes.nextSetBit(length - 1));
        assertEquals(-1, excludes.nextSetBit(length));
    }

    @Test
    public void testGetIncludes() {
        final Rule a = getRule("test*123*abc*");
        final Rule b = getRule("*test*abcd*");
        final Rule c = getRule("*123*test");
        final Rule d = getRule("*123*");
        final Rule[] rules = {a, b, c, d};

        final UserAgentParserImpl parser = new UserAgentParserImpl(rules, Collections.emptyList());

        final Filter startsWithTest = parser.createPrefixFilter("test");
        final Filter containsTest = parser.createContainsFilter("test");
        final Filter containsNumbers = parser.createContainsFilter("123");
        final Filter[] filters = {startsWithTest, containsTest, containsNumbers};

        final SearchableString useragent = new SearchableString("useragent_test_string");
        final BitSet includeRules = parser.getIncludeRules(useragent, filters);

        // b should be checked
        assertEquals(1, includeRules.nextSetBit(0));
        // No further rules to check
        assertEquals(-1, includeRules.nextSetBit(2));

        final SearchableString numberString = new SearchableString("123456");
        final BitSet numberIncludes = parser.getIncludeRules(numberString, filters);

        // Only d should be checked
        assertEquals(3, numberIncludes.nextSetBit(0));
        // No further rules to check
        assertEquals(-1, numberIncludes.nextSetBit(4));
    }

    @Test
    public void testGetOrderedRules() {
        final Rule a = getRule("a");
        final Rule b = getRule("b");
        final Rule aa = getRule("aa");
        final Rule bb = getRule("bb");

        // Sort by size first, then lexicographical
        final Rule[] expected = new Rule[]{aa, bb, a, b};

        final Rule[] rules = {bb, aa, b, a};
        assertArrayEquals(expected, getOrderedRules(rules));

        final Rule[] rulesAlt = {bb, a, b, aa};
        assertArrayEquals(expected, getOrderedRules(rulesAlt));
    }

    private Rule getRule(final String pattern) {
        final UserAgentFileParser parser = new UserAgentFileParser();
        final Rule rule = parser.createRule(pattern, DEFAULT, Collections.emptyList());
        assertEquals(pattern, rule.getPattern());
        return rule;
    }
}