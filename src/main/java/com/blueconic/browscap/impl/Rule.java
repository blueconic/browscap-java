package com.blueconic.browscap.impl;

import com.blueconic.browscap.Capabilities;

/**
 * Instances of this class represent a line of the browscap data. This class is responsible for checking a potential
 * match and for supplying the corresponding browser properties.
 */
class Rule {

    // The properties of the matching pattern
    private final Literal myPrefix;
    private final Literal[] mySuffixes;
    private final Literal myPostfix;

    // The size of the pattern
    private final int mySize;

    // The browser properties
    private final Capabilities myCapabilities;

    /**
     * Creates a new rule.
     * @param prefix The prefix of the matching pattern, potentially <code>null</code>
     * @param suffixes The required substrings separated by wildcards, potentially <code>null</code> to indicate no
     *            wildcards
     * @param postfix The postfix of the matching pattern, potentially <code>null</code>
     * @param pattern The original string representation of the matching pattern
     * @param capabilities The browser properties for this rule
     */
    Rule(final Literal prefix, final Literal[] suffixes, final Literal postfix, final String pattern,
            final Capabilities capabilities) {
        myPrefix = prefix;
        mySuffixes = suffixes;
        myPostfix = postfix;
        myCapabilities = capabilities;
        mySize = pattern.length();
    }

    /**
     * Return the prefix.
     * @return the prefix, possibly <code>null</code>
     */
    Literal getPrefix() {
        return myPrefix;
    }

    /**
     * The required substrings separated by wildcards, potentially <code>null</code> to indicate no wildcards
     * @return The required substrings.
     */
    Literal[] getSuffixes() {
        return mySuffixes;
    }

    /**
     * Return the postfix.
     * @return the postfix, possibly <code>null</code>
     */
    Literal getPostfix() {
        return myPostfix;
    }

    /**
     * Tests whether this rule needs a specific string in the useragent to match.
     * @return <code>true</code> if this rule can't match without the specific substring, false otherwise.
     */
    boolean requires(final String value) {
        if (requires(myPrefix, value) || requires(myPostfix, value)) {
            return true;
        }

        if (mySuffixes == null) {
            return false;
        }
        for (final Literal suffix : mySuffixes) {
            if (requires(suffix, value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean requires(final Literal literal, final String value) {
        return literal != null && literal.toString().contains(value);
    }

    Capabilities getCapabilities() {
        return myCapabilities;
    }

    int getSize() {
        return mySize;
    }

    final boolean matches(final SearchableString value) {

        // Inclusive
        final int start;
        if (myPrefix == null) {
            start = 0;
        } else if (value.startsWith(myPrefix)) {
            start = myPrefix.getLength();
        } else {
            return false;
        }

        // Inclusive
        final int end;
        if (myPostfix == null) {
            end = value.getSize() - 1;
        } else if (value.endsWith(myPostfix)) {
            end = value.getSize() - 1 - myPostfix.getLength();
        } else {
            return false;
        }

        return checkWildCards(value, mySuffixes, start, end);
    }

    // Static for inline (2x)
    static boolean checkWildCards(final SearchableString value, final Literal[] suffixes, final int start,
            final int end) {

        if (suffixes == null) {
            // No wildcards
            return start == end + 1;
        }

        // One wildcard
        if (suffixes.length == 0) {
            return start <= end + 1;
        }

        int from = start;
        for (final Literal suffix : suffixes) {

            final int match = checkWildCard(value, suffix, from);
            if (match == -1) {
                return false;
            }

            from = suffix.getLength() + match;
            if (from > end + 1) {
                return false;
            }
        }

        return true;
    }

    // Return found index or -1
    private static int checkWildCard(final SearchableString value, final Literal suffix, final int start) {
        for (final int index : value.getIndices(suffix)) {
            if (index >= start) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Returns the reconstructed original pattern.
     * @return the reconstructed original pattern
     */
    String getPattern() {
        final StringBuilder result = new StringBuilder();

        if (myPrefix != null) {
            result.append(myPrefix);
        }
        if (mySuffixes != null) {
            result.append("*");
            for (final Literal sub : mySuffixes) {
                result.append(sub);
                result.append("*");
            }
        }
        if (myPostfix != null) {
            result.append(myPostfix);
        }
        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getPattern() + " : " + myCapabilities;
    }
}
