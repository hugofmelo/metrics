package ufrn.dimap.lets.exceptionalinterface;

public class InvalidStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidStateException(String message) {
		super(message);
	}
}
