package exceptions;

public class InvalidRaceConfigurationException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidRaceConfigurationException(String message) {
        super(message);
    }
}
