package solver;

/**
 * Class for the combined linear conflict manhattan distance heuristic
 */
public class LinearConflictWithMD extends Heuristic {
    /**
     * Counts conflicts on rows and columns
     *
     * @param board          Board Object
     * @param rowConflict    Row array with given misplaced tiles
     * @param columnConflict Column array with given misplaced tiles
     * @return Total conflicts
     */
    private static int getConflicts(Board board, int[][] rowConflict, int[][] columnConflict) {
        int rowConflicts = 0;
        int columnConflicts = 0;
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_LENGTH - 1; j++) {
                //get row conflicts
                if (rowConflict[i][j] == i) {
                    for (int k = j + 1; k < Board.BOARD_LENGTH; k++) {
                        if (board.getState()[i][j] > board.getState()[i][k] && rowConflict[i][k] == i) {
                            rowConflicts += 2;
                        }
                    }
                }
                //get column conflicts
                if (columnConflict[i][j] == j) {
                    for (int k = i + 1; k < Board.BOARD_LENGTH; k++) {
                        if (board.getState()[i][j] > board.getState()[k][j] && columnConflict[k][j] == j) {
                            columnConflicts += 2;
                        }
                    }
                }
            }
        }
        return rowConflicts + columnConflicts;
    }

    /**
     * Calculates the combined linear conflict manhattan distance of the given board
     * Linear Conflict is when Two tiles ti and tj are in a linear conflict if ti
     * and tj are in the same line, the goal position of ti and tj are both in
     * that line, ti is to the right of tj, and the goal position of ti is
     * to the left of the goal position of tj.
     *
     * @param board Board Object
     * @return combined linear conflict manhattan distance count
     */
    @Override
    public int calculate(Board board) {
        //indicates a tile which is not in goal tile
        int[][] rowConflict = new int[Board.BOARD_LENGTH][Board.BOARD_LENGTH];
        int[][] columnConflict = new int[Board.BOARD_LENGTH][Board.BOARD_LENGTH];
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            for (int j = 0; j < Board.BOARD_LENGTH; j++) {
                //set goal row and column of tile
                if (board.getState()[i][j] != 0) {
                    rowConflict[i][j] = (board.getState()[i][j] - 1) / Board.BOARD_LENGTH;
                    columnConflict[i][j] = (board.getState()[i][j] - 1) % Board.BOARD_LENGTH;
                } else {
                    //indicates the blank tile with -1 to ensure blank tile is not counted as conflict
                    rowConflict[i][j] = -1;
                    columnConflict[i][j] = -1;
                }
            }
        }
        Heuristic manhattan = new ManhattanDistance();
//            System.out.println(Arrays.deepToString(rowConflict));
//            System.out.println(Arrays.deepToString(columnConflict));
        return manhattan.calculate(board) + getConflicts(board, rowConflict, columnConflict);
    }

    @Override
    public String getName() {
        return "Linear Conflict";
    }
}

