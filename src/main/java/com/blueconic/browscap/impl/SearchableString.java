package com.blueconic.browscap.impl;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents a searchable useragent strings. It relies and simple char arrays for low memory use and fast
 * operations. It provided methods for finding substrings and provides caches for better performance.
 */
class SearchableString {

    private static final int[] EMPTY = new int[0];
    private static final int[][] SINGLE_VALUES = getSingleValues();

    private final char[] myChars;
    private final int[][] myIndices;
    private final Cache myPrefixCache = new Cache();
    private final Cache myPostfixCache = new Cache();

    // Reusable buffer for findIndices
    private final int[] myBuffer;

    /**
     * Creates a new instance for the specified string value.
     * @param stringValue The user agent string
     * @param maxIndex The number of unique literals
     */
    SearchableString(final String stringValue, final int maxIndex) {
        myChars = stringValue.toCharArray();
        myIndices = new int[maxIndex][];
        myBuffer = new int[myChars.length];
    }

    /**
     * Returns the size of this instance.
     * @return The size
     */
    int getSize() {
        return myChars.length;
    }

    /**
     * Indicates whether this instance starts with the specified prefix.
     * @param literal The prefix that should be tested
     * @return <code>true</code> if the argument represents the prefix of this instance, <code>false</code> otherwise.
     */
    boolean startsWith(final Literal literal) {

        // Check whether the answer is already in the cache
        final int index = literal.getIndex();
        final Boolean cached = myPrefixCache.get(index);
        if (cached != null) {
            return cached.booleanValue();
        }

        // Get the answer and cache the result
        final boolean result = literal.matches(myChars, 0);
        myPrefixCache.set(index, result);
        return result;
    }

    /**
     * Indicates whether this instance ends with the specified postfix.
     * @param literal The postfix that should be tested
     * @return <code>true</code> if the argument represents the postfix of this instance, <code>false</code> otherwise.
     */
    boolean endsWith(final Literal literal) {

        // Check whether the answer is already in the cache
        final int index = literal.getIndex();
        final Boolean cached = myPostfixCache.get(index);
        if (cached != null) {
            return cached.booleanValue();
        }

        // Get the answer and cache the result
        final boolean result = literal.matches(myChars, myChars.length - literal.getLength());
        myPostfixCache.set(index, result);
        return result;
    }

    /**
     * Returns all indices where the literal argument can be found in this String. Results are cached for better
     * performance.
     * @param literal The string that should be found
     * @return all indices where the literal argument can be found in this String.
     */
    int[] getIndices(final Literal literal) {

        // Check whether the answer is already in the cache
        final int index = literal.getIndex();
        final int[] cached = myIndices[index];
        if (cached != null) {
            return cached;
        }

        // Find all indices
        final int[] values = findIndices(literal);
        myIndices[index] = values;
        return values;
    }

    /**
     * Returns all indices where the literal argument can be found in this String.
     * @param literal The string that should be found
     * @return all indices where the literal argument can be found in this String.
     */
    private int[] findIndices(final Literal literal) {

        int count = 0;
        final char s = literal.getFirstChar();
        for (int i = 0; i < myChars.length; i++) {

            // Check the first char for better performance and check the complete string
            if ((myChars[i] == s || s == '?') && literal.matches(myChars, i)) {

                // This index matches
                myBuffer[count] = i;
                count++;
            }
        }

        // Check whether any match has been found
        if (count == 0) {
            return EMPTY;
        }

        // Use an existing array
        if (count == 1 && myBuffer[0] < SINGLE_VALUES.length) {
            final int index = myBuffer[0];
            return SINGLE_VALUES[index];
        }

        // Copy the values
        final int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = myBuffer[i];
        }
        return values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new String(myChars);
    }

    private static final int[][] getSingleValues() {
        final int[][] result = new int[1024][];
        for (int i = 0; i < result.length; i++) {
            result[i] = new int[]{i};
        }
        return result;
    }

    /** Compact cache for boolean values. */
    static class Cache {

        // The boolean values
        private final BitSet myValues = new BitSet();

        // Indicates whether a value has been stored for an index
        private final BitSet myIsKnown = new BitSet();

        /**
         * Gets the cached value for the specified index.
         * @param index The index of the requested value
         * @return The cached boolean value, or <code>null</code> if no value is present in the cache.
         */
        Boolean get(final int index) {

            // Check whether a true value has been set
            if (myValues.get(index)) {
                return Boolean.TRUE;
            }

            // Check whether any value has been stored
            if (myIsKnown.get(index)) {
                return Boolean.FALSE;
            }

            // No value found
            return null;
        }

        /**
         * Set the value in the cache.
         * @param index The index for the stored value
         * @param flag The actual value
         */
        void set(final int index, final boolean flag) {

            // Store the value
            myValues.set(index, flag);

            // Store the fact the value has been stored
            myIsKnown.set(index, true);
        }
    }
}

/**
 * This combines a String value with a unique int value. The int value is used for caching of results.
 */
class Literal {

    // The actual string data
    private final char[] myCharacters;

    // The unique index for this instance
    private final int myIndex;

    /**
     * Creates a new instance with the specified non-empty value.
     * @param value The String value
     * @param index The unique index for this instance
     */
    Literal(final String value, final int index) {
        myCharacters = value.toCharArray();
        myIndex = index;
    }

    /**
     * Returns the first character for quick checks.
     * @return the first character
     */
    char getFirstChar() {
        return myCharacters[0];
    }

    /**
     * Returns the size of this instance.
     * @return The size of this instance
     */
    int getLength() {
        return myCharacters.length;
    }

    /**
     * Checks whether the value represents a complete substring from the from index.
     * @param from The start index of the potential substring
     * @return <code>true</code> If the arguments represent a valid substring, <code>false</code> otherwise.
     */
    boolean matches(final char[] value, final int from) {

        // Check the bounds
        final int len = myCharacters.length;
        if (len + from > value.length || from < 0) {
            return false;
        }

        // Bounds are ok, check all characters.
        // Allow question marks to match any character
        for (int i = 0; i < len; i++) {
            if (myCharacters[i] != value[i + from] && myCharacters[i] != '?') {
                return false;
            }
        }

        // All characters match
        return true;
    }

    /**
     * Returns the unique index of this instance.
     * @return the unique index of this instance.
     */
    final int getIndex() {
        return myIndex;
    }

    private static boolean contains(final char[] characters, final char value) {

        for (final char c : characters) {
            if (c == value) {
                return true;
            }
        }
        return false;
    }

    boolean requires(final String value) {
        final int len = value.length();
        if (len == 1) {
            return contains(myCharacters, value.charAt(0));
        }

        if (len > myCharacters.length) {
            return false;
        }

        return toString().contains(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new String(myCharacters);
    }
}

class LiteralDomain {

    // Keep track of the total number of instances
    private final AtomicInteger myNrOfInstances = new AtomicInteger();

    Literal createLiteral(final String contents) {
        return new Literal(contents, myNrOfInstances.getAndAdd(1));
    }

    SearchableString getSearchableString(final String contents) {
        final int maxIndex = myNrOfInstances.get() + 1;

        return new SearchableString(contents, maxIndex);
    }
}
