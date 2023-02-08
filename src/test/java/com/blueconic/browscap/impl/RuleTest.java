package com.blueconic.browscap.impl;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.impl.UserAgentFileParserTest.DEFAULT;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuleTest {

    private UserAgentFileParser myParser;

    @BeforeEach
    void setup() {
        myParser = new UserAgentFileParser(singleton(BROWSER));
    }

    @Test
    void testLiteralExpression() {
        final Rule rule = getRule("a");

        assertTrue(matches(rule, "a"));

        assertFalse(matches(rule, "b"));
        assertFalse(matches(rule, "ab"));
        assertFalse(matches(rule, "ba"));
    }

    @Test
    void testLiteralQuestionMark() {
        final Rule literal = getRule("?a");

        assertTrue(matches(literal, "aa"));
        assertTrue(matches(literal, "ba"));

        assertFalse(matches(literal, "ab"));
        assertFalse(matches(literal, "a"));
        assertFalse(matches(literal, "aaa"));

        final Rule literal2 = getRule("?a?");

        assertTrue(matches(literal2, "bac"));
        assertFalse(matches(literal2, "a"));
    }

    @Test
    void testSuffix() {

        final Rule expression = getRule("*abc*");

        assertTrue(matches(expression, "abc"));
        assertTrue(matches(expression, "1abc3"));
        assertTrue(matches(expression, "1abababc3"));

        assertFalse(matches(expression, "ab"));
        assertFalse(matches(expression, "1bc2"));
        assertFalse(matches(expression, "1ab2"));
    }

    @Test
    void testPrefix() {
        final Rule expression = getRule("abc*");

        assertTrue(matches(expression, "abc"));
        assertTrue(matches(expression, "abcd"));

        assertFalse(matches(expression, "ab"));
        assertFalse(matches(expression, "1abc"));
    }

    @Test
    void testPostfix() {

        final Rule expression = getRule("*abc");

        assertTrue(matches(expression, "abc"));
        assertTrue(matches(expression, "1abc"));

        assertFalse(matches(expression, "abcd"));
        assertFalse(matches(expression, "ab"));
    }

    @Test
    void testPreFixMultiple() {

        final Rule expression = getRule("a*z");

        assertTrue(matches(expression, "az"));
        assertTrue(matches(expression, "alz"));

        assertFalse(matches(expression, ""));
        assertFalse(matches(expression, "ab"));

        final Rule multiple = getRule("a*b*z");

        assertTrue(matches(multiple, "abz"));
        assertTrue(matches(multiple, "a_b_z"));
        assertTrue(matches(multiple, "ababz"));

        assertFalse(matches(multiple, ""));
        assertFalse(matches(multiple, "abz1"));

        // Test overlap
        assertFalse(matches(getRule("aa*aa"), "aaa"));
        assertFalse(matches(getRule("*aa*aa"), "aaa"));
    }

    @Test
    void testSuffixMultiple() {

        final Rule rule = getRule("*a*z*");

        assertTrue(matches(rule, "az"));
        assertTrue(matches(rule, "alz"));
        assertTrue(matches(rule, "1alz3"));
        assertTrue(matches(rule, "AAAaAAAAzAAAA"));

        assertFalse(matches(rule, ""));
        assertFalse(matches(rule, "ab"));
        assertFalse(matches(rule, "za"));
    }

    @Test
    void testRequires() {
        final Rule rule = getRule("*abc*def*");
        assertTrue(rule.requires("abc"));
        assertTrue(rule.requires("def"));
        assertTrue(rule.requires("bc"));
        assertFalse(rule.requires("abcdef"));

        final Rule prepost = getRule("abc*def");
        assertTrue(prepost.requires("abc"));
        assertTrue(prepost.requires("def"));
        assertTrue(prepost.requires("bc"));
    }

    private Rule getRule(final String pattern) {
        final Rule rule = myParser.createRule(pattern, DEFAULT);
        assertEquals(pattern, rule.getPattern());
        return rule;
    }

    private boolean matches(final Rule rule, final String useragent) {
        final SearchableString searchableString = myParser.getDomain().getSearchableString(useragent);
        return rule.matches(searchableString);
    }
}