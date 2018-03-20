package org.sql2o;

/**
 * Represents an exception thrown by Sql2o.
 */
public class Sql2oException extends RuntimeException {
    
    private static final long serialVersionUID = -7941623550038122757L;

	public Sql2oException() {
    }

    public Sql2oException(String message) {
        super(message);
    }

    public Sql2oException(String message, Throwable cause) {
        super(message, cause);
    }

    public Sql2oException(Throwable cause) {
        super(cause);
    }
}
