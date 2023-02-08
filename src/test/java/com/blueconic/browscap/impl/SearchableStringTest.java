package com.blueconic.browscap.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.blueconic.browscap.impl.SearchableString.Cache;
import org.junit.jupiter.api.Test;

class SearchableStringTest {

    @Test
    void testSearchableString() {

        final LiteralDomain domain = new LiteralDomain();

        final Literal abc = domain.createLiteral("abc");
        final Literal ab = domain.createLiteral("ab");

        final String stringValue = "abababc";
        final SearchableString cache = domain.getSearchableString(stringValue);

        assertTrue(cache.startsWith(ab));
        assertFalse(cache.startsWith(abc));
        // Test caching path
        assertFalse(cache.startsWith(abc));

        assertTrue(cache.endsWith(abc));
        assertFalse(cache.endsWith(ab));
        // Test caching path
        assertFalse(cache.endsWith(ab));

        // Test simple methods
        assertEquals(stringValue, cache.toString());
        assertEquals(stringValue.length(), cache.getSize());
    }

    @Test
    void testGetIndices() {
        final LiteralDomain domain = new LiteralDomain();

        final Literal abc = domain.createLiteral("abc");
        final Literal ab = domain.createLiteral("ab");
        final Literal anyChar = domain.createLiteral("?ab");
        final Literal noMatch = domain.createLiteral("aaaaaaaaaaaaaaaaaa");

        final SearchableString cache = domain.getSearchableString("abababc");

        assertArrayEquals(new int[]{4}, cache.getIndices(abc));
        assertArrayEquals(new int[]{0, 2, 4}, cache.getIndices(ab));

        assertArrayEquals(new int[]{1, 3}, cache.getIndices(anyChar));

        assertArrayEquals(new int[0], cache.getIndices(noMatch));

        // Test caching
        final int[] indices = cache.getIndices(ab);
        assertSame(indices, cache.getIndices(ab));
    }

    @Test
    void testCache() {
        final Cache cache = new Cache();

        assertNull(cache.get(0));
        cache.set(0, true);
        assertTrue(cache.get(0));

        assertNull(cache.get(1));
        cache.set(1, false);
        assertFalse(cache.get(1));
    }

    @Test
    void testLiteralBasics() {

        final LiteralDomain domain = new LiteralDomain();

        final String input = "abcdef";
        final Literal literal = domain.createLiteral(input);
        assertEquals(input.length(), literal.getLength());
        assertEquals('a', literal.getFirstChar());
        assertEquals(input, literal.toString());
    }

    @Test
    void testLiteralMatches() {

        final LiteralDomain domain = new LiteralDomain();

        final String input = "def";
        final Literal literal = domain.createLiteral(input);

        // Test for matches also with invalid bounds
        final char[] search = "abcdef".toCharArray();
        assertTrue(literal.matches(search, 3));
        assertFalse(literal.matches(search, 0));
        assertFalse(literal.matches(search, 5));
        assertFalse(literal.matches(search, -10));
        assertFalse(literal.matches(search, 100));

        final Literal joker = domain.createLiteral("d?f");
        assertTrue(joker.matches(search, 3));
        assertFalse(literal.matches(search, 0));
        assertFalse(literal.matches(search, 5));
    }
}
