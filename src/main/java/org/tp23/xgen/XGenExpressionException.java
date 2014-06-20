package org.tp23.xgen;

/**
 * Throw for syntax errors in xGen paths.
 * @author teknopaul
 */
public class XGenExpressionException extends Exception {

	private static final long serialVersionUID = -5363660031780012330L;

	public XGenExpressionException() {
		super();
	}

	public XGenExpressionException(String message, Throwable cause) {
		super(message, cause);
	}

	public XGenExpressionException(String message) {
		super(message);
	}

	public XGenExpressionException(Throwable cause) {
		super(cause);
	}

}
