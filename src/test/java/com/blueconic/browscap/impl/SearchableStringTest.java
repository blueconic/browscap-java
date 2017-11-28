package com.blueconic.browscap.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blueconic.browscap.impl.SearchableString.Cache;

public class SearchableStringTest {

    @Test
    public void testSearchableString() {
        final Literal abc = new Literal("abc");
        final Literal ab = new Literal("ab");

        final String stringValue = "abababc";
        final SearchableString cache = new SearchableString(stringValue);

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
    public void testGetIndices() {
        final Literal abc = new Literal("abc");
        final Literal ab = new Literal("ab");
        final Literal anyChar = new Literal("?ab");
        final Literal noMatch = new Literal("aaaaaaaaaaaaaaaaaa");

        final SearchableString cache = new SearchableString("abababc");

        assertArrayEquals(new int[]{4}, cache.getIndices(abc));
        assertArrayEquals(new int[]{0, 2, 4}, cache.getIndices(ab));

        assertArrayEquals(new int[]{1, 3}, cache.getIndices(anyChar));

        assertArrayEquals(new int[0], cache.getIndices(noMatch));

        // Test caching
        final int[] indices = cache.getIndices(ab);
        assertSame(indices, cache.getIndices(ab));
    }

    @Test
    public void testCache() {
        final Cache cache = new Cache();

        assertNull(cache.get(0));
        cache.set(0, true);
        assertTrue(cache.get(0));

        assertNull(cache.get(1));
        cache.set(1, false);
        assertFalse(cache.get(1));
    }

    @Test
    public void testLiteralBasics() {
        final String input = "abcdef";
        final Literal literal = new Literal(input);
        assertEquals(input.length(), literal.getLength());
        assertEquals('a', literal.getFirstChar());
        assertEquals(input, literal.toString());
    }

    @Test
    public void testLiteralMatches() {
        final String input = "def";
        final Literal literal = new Literal(input);

        // Test for matches also with invalid bounds
        final char[] search = "abcdef".toCharArray();
        assertTrue(literal.matches(search, 3));
        assertFalse(literal.matches(search, 0));
        assertFalse(literal.matches(search, 5));
        assertFalse(literal.matches(search, -10));
        assertFalse(literal.matches(search, 100));

        final Literal joker = new Literal("d?f");
        assertTrue(joker.matches(search, 3));
        assertFalse(literal.matches(search, 0));
        assertFalse(literal.matches(search, 5));
    }
}
