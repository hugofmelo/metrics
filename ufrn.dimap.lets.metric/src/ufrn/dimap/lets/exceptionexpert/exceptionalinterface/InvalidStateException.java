package ufrn.dimap.lets.exceptionexpert.exceptionalinterface;

public class InvalidStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidStateException(String message) {
		super(message);
	}
}
