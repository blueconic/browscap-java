package com.blueconic.browscap.impl;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.impl.UserAgentFileParserTest.DEFAULT;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RuleTest {

    @Test
    public void testLiteralExpression() {
        final Rule rule = getRule("a");

        assertTrue(matches(rule, "a"));

        assertFalse(matches(rule, "b"));
        assertFalse(matches(rule, "ab"));
        assertFalse(matches(rule, "ba"));
    }

    @Test
    public void testLiteralQuestionMark() {
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
    public void testSuffix() {

        final Rule expression = getRule("*abc*");

        assertTrue(matches(expression, "abc"));
        assertTrue(matches(expression, "1abc3"));
        assertTrue(matches(expression, "1abababc3"));

        assertFalse(matches(expression, "ab"));
        assertFalse(matches(expression, "1bc2"));
        assertFalse(matches(expression, "1ab2"));
    }

    @Test
    public void testPrefix() {
        final Rule expression = getRule("abc*");

        assertTrue(matches(expression, "abc"));
        assertTrue(matches(expression, "abcd"));

        assertFalse(matches(expression, "ab"));
        assertFalse(matches(expression, "1abc"));
    }

    @Test
    public void testPostfix() {

        final Rule expression = getRule("*abc");

        assertTrue(matches(expression, "abc"));
        assertTrue(matches(expression, "1abc"));

        assertFalse(matches(expression, "abcd"));
        assertFalse(matches(expression, "ab"));
    }

    @Test
    public void testPreFixMultiple() {

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
    public void testSuffixMultiple() {

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
    public void testRequires() {
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
        final UserAgentFileParser parser = new UserAgentFileParser(singleton(BROWSER));
        final Rule rule = parser.createRule(pattern, DEFAULT);
        assertEquals(pattern, rule.getPattern());
        return rule;
    }

    private boolean matches(final Rule rule, final String useragent) {
        return rule.matches(new SearchableString(useragent));
    }
}