package org.sql2o.converters;

/**
 * Represents an exception thrown from a converter.
 */
public class ConverterException extends Exception{

    private static final long serialVersionUID = -6515158190992901520L;

	public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}
