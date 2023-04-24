package solver;

/**
 *
 */
public class PuzzleException extends RuntimeException {
    public PuzzleException(String message) {
        super(message);
    }

    public PuzzleException() {
        super("Something went wrong");
    }
}
