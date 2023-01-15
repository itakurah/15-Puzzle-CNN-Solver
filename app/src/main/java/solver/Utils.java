package solver;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class for helper methods used in other classes
 */
public abstract class Utils {
    static int[][] goal = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 0}};

    /**
     * A puzzle is solvable if:
     * 1. The blank is in an even row counting from
     * the bottom row and the number of inversions is odd
     * 2. The blank is in an odd row counting from
     * the bottom row and the number of inversions is even
     *
     * @param state to be checked for solvable state
     * @return true when the board is solvable
     */
    public static boolean isSolvable(int[][] state) {
        return (((getInversionCount(state) + getBlankTileCount(state)) % 2) == 0);
    }

    public static boolean isSolution(Board board) {
        return board.equals(new Board(goal));
    }

    /**
     * Returns inversion count
     *
     * @param state array containing puzzle state
     * @return Inversion count
     */
    public static int getInversionCount(int[][] state) {
        int[] copy1D = arrayToOneDimension(state);
        int count = 0;
        for (int i = 0; i < copy1D.length - 1; i++) {
            for (int j = i + 1; j < copy1D.length; j++) {
                if ((copy1D[i] > copy1D[j]) & (copy1D[j] > 0))
                    count++;
            }
        }
        return count;
    }

    /**
     * Flattens an 2d array to one dimension
     *
     * @param arrayToFlatten Array to flatten
     * @return flatted array
     */
    public static int[] arrayToOneDimension(int[][] arrayToFlatten) {
        return Stream.of(arrayToFlatten)
                .flatMapToInt(IntStream::of)
                .toArray();
    }

    /**
     * Returns blank tile count
     *
     * @param state array
     * @return Returns true if blank tile is even
     */
    public static int getBlankTileCount(int[][] state) {
        for (int i = 0; i < state.length; i++)
            for (int j = 0; j < state.length; j++) {
                if (state[i][j] == 0) {
                    return 4 - (i + 1);
                }
            }
        throw new IllegalArgumentException("state does not contain 0");
    }





    /**
     * Returns the moves required to solve the given puzzle
     *
     * @param path Path list with all moves
     * @return String with all moves
     */
    public static String getMoves(LinkedList<Board> path) {
        StringBuilder turnsToSolve = new StringBuilder();
        if (path.get(0).getParent() == null) {
            return "initial board is goal board";
        }
        for (int i = path.size()-2; i >= 0; i--) {
            turnsToSolve.append(path.get(i).getMove()).append("-");
        }
        turnsToSolve = new StringBuilder(turnsToSolve.substring(0, turnsToSolve.length() - 1));
        return turnsToSolve.toString();
    }

    /**
     * Print the result of solved board
     *
     * @param algorithmType       Type of algorithm used AStar or IDAStar
     * @param heuristicType       Type of heuristic used
     * @param numOfExpandedBoards Total number of created boards
     * @param memory              Total memory used during execution time
     * @param runtime             Total runtime of algorithm
     */
    public static void printResults(String algorithmType, HeuristicType heuristicType, int numOfExpandedBoards, LinkedList<Board> listBoards, long memory, long runtime) {
        System.out.println("Algorithm: " + algorithmType);
        System.out.println("Heuristic: " + heuristicType);
        System.out.println("Expanded boards: " + numOfExpandedBoards);
        System.out.println("Depth: " + listBoards.getFirst().getgScore());
        System.out.println("Memory used: " + memory + " KB");
        System.out.println("Run time: " + runtime);
        System.out.println("Moves: " + Utils.getMoves(listBoards));
        System.out.println("Board solved!");
        System.out.println("----------------------");
    }

    /**
     * Checks if the given board is valid
     *
     * @param board Board object
     */
    public static void checkInput(Board board) throws Exception{
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }
        if (!isStateValid(board.getState())) {
            throw new IllegalArgumentException("Board is invalid\nusage: e.g.: 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 0");
        }
    }

    /**
     * Checks if the given board is valid
     * Checks for null
     * Checks for length of state array
     * Checks for individual array size
     * Checks for duplicates and for numbers outside 1-15
     *
     * @param state array containing puzzle state
     * @return true if state is valid
     */
    public static boolean isStateValid(int[][] state) {
        if (state == null) {
            throw new NullPointerException("state can't be null");
        }
        if ((state[0].length | state[1].length | state[2].length | state[3].length) != 4 || state.length != 4) {
            return false;
        }
        if (!isSolvable(state)) {
            return false;
        }
        int[] boardOneDim = arrayToOneDimension(state);
        int[] goalOneDim = arrayToOneDimension(goal);
        Integer[] boxedBoard = Arrays.stream(boardOneDim).boxed().toArray(Integer[]::new);
        Integer[] boxedGoal = Arrays.stream(goalOneDim).boxed().toArray(Integer[]::new);
        HashSet<Integer> setBoard = new HashSet<>(Arrays.asList(boxedBoard));
        HashSet<Integer> setGoal = new HashSet<>(Arrays.asList(boxedGoal));
        return setBoard.equals(setGoal);
    }
}
