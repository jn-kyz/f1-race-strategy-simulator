package exceptions;

public class InvalidStrategyException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidStrategyException(String message) {
        super(message);
    }
}
