package solver;

/**
 * Class for the manhattan distance heuristic
 */
public abstract class ManhattanDistance implements IHeuristic {
    /**
     * Calculates the manhattan distance of the given board
     * Manhattan distance is the sum of moves which a tile at least needs to reach their goal position
     *
     * @param board Board Object
     * @return manhattan distance count
     */
    public static int calculate(Board board) {
        int mdCount = 0;
        int goalPosition = 0;
        for (int i = 0; i < board.getROW(); i++) {
            for (int j = 0; j < board.getCOLUMN(); j++) {
                int number = board.getState()[i][j];
                goalPosition++;
                if (number != 0 && number != goalPosition) {
                    mdCount = mdCount + Math.abs(i - ((number - 1) / board.getROW()))
                            + Math.abs(j - ((number - 1) % board.getCOLUMN()));
                }
            }
        }
        return mdCount;
    }
}
