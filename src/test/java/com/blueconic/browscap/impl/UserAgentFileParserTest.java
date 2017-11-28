package com.blueconic.browscap.impl;

import static com.blueconic.browscap.impl.CapabilitiesImpl.DEFAULT;
import static com.blueconic.browscap.impl.UserAgentFileParser.getParts;
import static com.blueconic.browscap.impl.UserAgentFileParser.getValue;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.junit.Test;

public class UserAgentFileParserTest {

    @Test
    public void testGetParts() {
        assertEquals(asList("*", "a", "*"), getParts("*a*"));
        assertEquals(asList("*", "abc", "*"), getParts("*abc*"));
        assertEquals(asList("*"), getParts("*"));
        assertEquals(asList("a"), getParts("a"));
    }

    @Test(expected = IllegalStateException.class)
    public void testLiteralException() {
        final UserAgentFileParser parser = new UserAgentFileParser();
        parser.createRule("", DEFAULT, Collections.emptyList());
    }

    @Test
    public void testCreateRule() {
        final UserAgentFileParser parser = new UserAgentFileParser();

        final Rule exact = parser.createRule("a", DEFAULT, Collections.emptyList());
        validate(exact, "a", null, null);

        final Rule wildcard = parser.createRule("*", DEFAULT, Collections.emptyList());
        validate(wildcard, null, new String[0], null);

        final Rule prefix = parser.createRule("abc*", DEFAULT, Collections.emptyList());
        validate(prefix, "abc", new String[0], null);

        final Rule postfix = parser.createRule("*abc", DEFAULT, Collections.emptyList());
        validate(postfix, null, new String[0], "abc");

        final Rule prePost = parser.createRule("abc*def", DEFAULT, Collections.emptyList());
        validate(prePost, "abc", new String[0], "def");

        final Rule suffix = parser.createRule("*abc*", DEFAULT, Collections.emptyList());
        validate(suffix, null, new String[]{"abc"}, null);

        final Rule expression = parser.createRule("*a*z*", DEFAULT, Collections.emptyList());
        validate(expression, null, new String[]{"a", "z"}, null);
    }

    void validate(final Rule rule, final String prefix, final String[] subs, final String postfix) {
        validate(prefix, rule.getPrefix());
        validate(postfix, rule.getPostfix());

        if (subs == null) {
            assertNull(rule.getSuffixes());
        } else {
            final Literal[] suffixes = rule.getSuffixes();
            assertEquals(subs.length, suffixes.length);
            for (int i = 0; i < subs.length; i++) {
                validate(subs[i], suffixes[i]);
            }
        }
    }

    void validate(final String stringValue, final Literal literal) {
        if (stringValue == null) {
            assertNull(literal);
        } else {
            assertEquals(stringValue, literal.toString());
        }
    }

    @Test
    public void testGetValue() {

        // Test missing values
        assertEquals("Unknown", getValue(null));
        assertEquals("Unknown", getValue(""));
        assertEquals("Unknown", getValue(" "));

        // Test trimming and interning
        final String input = "Test";
        assertSame("Test", getValue(input + " "));
        assertSame("Test", getValue(" " + input));
    }
}