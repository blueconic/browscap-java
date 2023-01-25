package com.blueconic.browscap.impl;

import static com.blueconic.browscap.BrowsCapField.BROWSER;
import static com.blueconic.browscap.impl.UserAgentFileParser.getParts;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;

import com.blueconic.browscap.Capabilities;
import org.junit.jupiter.api.Test;

class UserAgentFileParserTest {

    static final Capabilities DEFAULT = new UserAgentFileParser(singleton(BROWSER)).getDefaultCapabilities();

    @Test
    void testGetParts() {
        assertEquals(asList("*", "a", "*"), getParts("*a*"));
        assertEquals(asList("*", "abc", "*"), getParts("*abc*"));
        assertEquals(asList("*"), getParts("*"));
        assertEquals(asList("a"), getParts("a"));
    }

    @Test
    void testLiteralException() {
        final UserAgentFileParser parser = new UserAgentFileParser(singleton(BROWSER));
        assertThrows(IllegalStateException.class, () -> parser.createRule("", DEFAULT));
    }

    @Test
    void testCreateRule() {
        final UserAgentFileParser parser = new UserAgentFileParser(singleton(BROWSER));

        final Rule exact = parser.createRule("a", DEFAULT);
        validate(exact, "a", null, null);

        final Rule wildcard = parser.createRule("*", DEFAULT);
        validate(wildcard, null, new String[0], null);

        final Rule prefix = parser.createRule("abc*", DEFAULT);
        validate(prefix, "abc", new String[0], null);

        final Rule postfix = parser.createRule("*abc", DEFAULT);
        validate(postfix, null, new String[0], "abc");

        final Rule prePost = parser.createRule("abc*def", DEFAULT);
        validate(prePost, "abc", new String[0], "def");

        final Rule suffix = parser.createRule("*abc*", DEFAULT);
        validate(suffix, null, new String[]{"abc"}, null);

        final Rule expression = parser.createRule("*a*z*", DEFAULT);
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
    void testGetValue() {

        final UserAgentFileParser parser = new UserAgentFileParser(emptySet());

        // Test missing values
        assertEquals("Unknown", parser.getValue(null));
        assertEquals("Unknown", parser.getValue(""));
        assertEquals("Unknown", parser.getValue(" "));

        // Test trimming and interning
        final String input = "Test";
        assertSame("Test", parser.getValue(input));
        assertSame("Test", parser.getValue(input + " "));
        assertSame("Test", parser.getValue(" " + input));
    }
}