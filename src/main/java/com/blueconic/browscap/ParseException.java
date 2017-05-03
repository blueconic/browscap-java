package com.blueconic.browscap;

/**
 * Exception which is thrown when a regular expression in the BrowsCap CSV cannot be parsed
 */
public class ParseException extends Exception {
    public ParseException(final String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;

}
