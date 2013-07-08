package pl.gdela.socomo.maven;

/**
 * Class used to change checked to unchecked exception.
 * http://googletesting.blogspot.com/2009/09/checked-exceptions-i-love-you-but-you.html
 */
public class LogRuntimeException extends RuntimeException {
	public LogRuntimeException() {
		super();
	}

	public LogRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LogRuntimeException(String message) {
		super(message);
	}

	public LogRuntimeException(Throwable cause) {
		super(cause);
	}
}
