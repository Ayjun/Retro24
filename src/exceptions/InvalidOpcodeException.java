package exceptions;

public class InvalidOpcodeException extends IllegalArgumentException {
	private static final long serialVersionUID = -6296576204319542922L;

	public InvalidOpcodeException(String message) {
		super(message);
	}
}
