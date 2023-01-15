package solver;

/**
 * Class for calculating heuristic count of a given board object
 */
public abstract class HeuristicSelection {
    /**
     * Returns Heuristic count of given board object
     *
     * @param board Board object
     * @param type  Heuristic type for solving puzzle
     * @return Hscore of given board
     */
    public static int getHeuristicScore(Board board, HeuristicType type) {
        if(type==HeuristicType.LCMD){
                return LinearConflictWithMD.calculate(board);
        }
        throw new IllegalArgumentException("Heuristic type not valid");
    }
}
