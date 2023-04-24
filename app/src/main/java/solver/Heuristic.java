package solver;

/**
 *
 */
public abstract class Heuristic {
    public abstract int calculate(Board board);

    public abstract String getName();
}
