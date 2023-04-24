package solver;

/**
 * Class for the manhattan distance heuristic
 */
public class ManhattanDistance extends Heuristic {
    /**
     * Calculates the manhattan distance of the given board
     * Manhattan distance is the sum of moves which a tile at least needs to reach their goal position
     *
     * @param board Board Object
     * @return manhattan distance count
     */
    @Override
    public int calculate(Board board) {
        int mdCount = 0;
        int goalPosition = 0;
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_LENGTH; j++) {
                int number = board.getState()[i][j];
                goalPosition++;
                if (number != 0 && number != goalPosition) {
                    mdCount = mdCount + Math.abs(i - ((number - 1) / Board.BOARD_LENGTH))
                            + Math.abs(j - ((number - 1) % Board.BOARD_LENGTH));
                }
            }
        }
        return mdCount;
    }

    @Override
    public String getName() {
        return "Manhattan Distance";
    }
}
