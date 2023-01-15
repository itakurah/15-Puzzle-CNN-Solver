package solver;

/**
 * Class for the combined linear conflict manhattan distance heuristic
 */
public abstract class LinearConflictWithMD implements IHeuristic {
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
    public static int calculate(Board board) {
        //indicates a tile which is not in goal tile
        int[][] rowConflict = new int[board.getROW()][board.getCOLUMN()];
        int[][] columnConflict = new int[board.getROW()][board.getCOLUMN()];
        for (int i = 0; i < board.getROW(); i++) {
            for (int j = 0; j < board.getCOLUMN(); j++) {
                //set goal row and column of tile
                if (board.getState()[i][j] != 0) {
                    rowConflict[i][j] = (board.getState()[i][j] - 1) / board.getROW();
                    columnConflict[i][j] = (board.getState()[i][j] - 1) % board.getCOLUMN();
                } else {
                    //indicates the blank tile with -1 to ensure blank tile is not counted as conflict
                    rowConflict[i][j] = -1;
                    columnConflict[i][j] = -1;
                }
            }
        }
//            System.out.println(Arrays.deepToString(rowConflict));
//            System.out.println(Arrays.deepToString(columnConflict));
        return ManhattanDistance.calculate(board) + getConflicts(board, rowConflict, columnConflict);
    }

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
        for (int i = 0; i < board.getROW(); i++) {
            for (int j = 0; j < board.getCOLUMN() - 1; j++) {
                //get row conflicts
                if (rowConflict[i][j] == i) {
                    for (int k = j + 1; k < board.getROW(); k++) {
                        if (board.getState()[i][j] > board.getState()[i][k] && rowConflict[i][k] == i) {
                            rowConflicts += 2;
                        }
                    }
                }
                //get column conflicts
                if (columnConflict[i][j] == j) {
                    for (int k = i + 1; k < board.getCOLUMN(); k++) {
                        if (board.getState()[i][j] > board.getState()[k][j] && columnConflict[k][j] == j) {
                            columnConflicts += 2;
                        }
                    }
                }
            }
        }
        return rowConflicts + columnConflicts;
    }
}

