package org.epos.api.exceptions;


public class IncorrectParameterException extends Exception { 
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IncorrectParameterException(String errorMessage) {
        super(errorMessage);
    }
}