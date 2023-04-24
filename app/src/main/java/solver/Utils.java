package solver;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Class for helper methods used in other classes
 */
public abstract class Utils {
    private static final int[][] goal = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 0}};

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
     * Returns shuffled board with given turns shuffled,
     * does not guarantee that the optimal solution is solvable
     * in at least given turns. Double check with algorithms.AStar solver.
     *
     * @param numberOfSteps Number of steps to be shuffled
     * @return Shuffled 2d array
     */
    public static int[][] getXTurnSolvableState(int numberOfSteps, Heuristic heuristic) {
        Board board = new Board(goal);
        board.setHeuristic(heuristic);
        HashSet<Board> alreadyStepped = new HashSet<>();
        Random rand = new Random();
        for (int i = 0; i < numberOfSteps; i++) {
            board.setSuccessors(board.generateSuccessors());
            board.getSuccessors().removeAll(alreadyStepped);
            alreadyStepped.add(board);
            if (board.getSuccessors().size() != 0) {
                board = board.getSuccessors().get(rand.nextInt(board.getSuccessors().size()));
            }
        }
        return copyState(board);
    }

    /**
     * Generates a random 15-Puzzle
     *
     * @return random puzzle
     */
    public static int[][] getRandomPuzzle() {
        int x = 4;
        int y = 4;
        int[][] puzzle = new int[x][y];

        do {
            List<Integer> list = IntStream.rangeClosed(0, x * y - 1)
                    .boxed()
                    .collect(Collectors.toList());
            Collections.shuffle(list);
            List<List<Integer>> puzzleList = IntStream.range(0, x)
                    .mapToObj(i -> list.subList(i * y, (i + 1) * y)).collect(Collectors.toList());
            int listCount = 0;
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < y; j++) {
                    puzzle[i][j] = puzzleList.get(i).get(j);
                    listCount++;
                }
            }
        } while (!isSolvable(puzzle));
        return puzzle;
    }

    /**
     * Copies state from given board and returns 2d array
     *
     * @param board Board object
     * @return Copied array of parent board
     */
    public static int[][] copyState(Board board) {
        int[][] copy = new int[board.getState().length][];
        for (int i = 0; i < Board.BOARD_LENGTH; i++) {
            copy[i] = Arrays.copyOf(board.getState()[i], board.getState().length);
        }
        return copy;
    }

    /**
     * Returns the moves required to solve the given puzzle
     *
     * @param board Board object
     * @return String with all moves
     */
    public static String getMoves(Board board) {
        StringBuilder turnsToSolve = new StringBuilder();
        if (board.getParent() == null) {
            return "initial board is goal board";
        }
        while (!(board.getParent() == null)) {
            turnsToSolve.append(board.getMove()).append("-");
            board = board.getParent();
        }

        turnsToSolve = new StringBuilder(turnsToSolve.substring(0, turnsToSolve.length() - 1));
        return turnsToSolve.reverse().toString();
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
        for (int i = path.size() - 2; i >= 0; i--) {
            turnsToSolve.append(path.get(i).getMove()).append("-");
        }
        turnsToSolve = new StringBuilder(turnsToSolve.substring(0, turnsToSolve.length() - 1));
        return turnsToSolve.toString();
    }

    /**
     * Used for generating mass puzzle with given depth
     * found puzzles are then stored in 'puzzles+(optimal turns to solve).txt'
     *
     * @param minTurnsToSolve Board with exact given number will be saved
     * @param numberOfSteps   Steps the board will be walked over note for best results
     *                        numberOfSteps should be the same number than minTurnsToSolve
     *                        or greater but then even or odd like minTurnsToSolve
     * @param bound           Bound for how many puzzles are being theoretically generated if found
     * @param heuristic       Type of the given heuristic
     * @throws FileNotFoundException being thrown when an error occurred
     *                               while writing or creating the puzzle file
     */
    public static void generatePuzzles(int minTurnsToSolve, int numberOfSteps, int bound, Heuristic heuristic) throws FileNotFoundException {
        File file = new File("puzzles" + minTurnsToSolve + ".txt");
        Set<Board> setBoards = new HashSet<>();
        PrintStream fileStream = new PrintStream(new FileOutputStream(file, true));
        for (int i = 0; i < bound; i++) {
            int[][] state = Utils.getXTurnSolvableState(numberOfSteps, heuristic);
            Board board = new Board(state);
            if (IDAStar.solve(board, heuristic, TimeUnit.NS, DebugMode.OFF).getDepth() == minTurnsToSolve) {
                setBoards.add(board);
                System.out.println(board);
            }
        }
        for (Board b : setBoards
        ) {
            fileStream.println(Arrays.deepToString(b.getState())
                    .replace("[", "")
                    .replace("]", "").replace(",", ""));
        }
    }

    public static int[][] getGoal() {
        return goal;
    }

    /**
     * Print the result of solved board
     *
     * @param algorithmType       Type of algorithm used AStar or IDAStar
     * @param heuristic           Type of heuristic used
     * @param numOfExpandedBoards Total number of created boards
     * @param openList            Total size of open list in AStar algorithm
     * @param closedList          size of closed list in AStar algorithm
     * @param currentBoard        Current board
     * @param memory              Total memory used during execution time
     * @param runtime             Total runtime of algorithm
     */
    public static void printResults(String algorithmType, Heuristic heuristic, int numOfExpandedBoards,
                                    PriorityQueue<Board> openList, Set<Board> closedList, Board currentBoard, LinkedList<Board> listBoards, long memory, long runtime) {
        System.out.println("Algorithm: " + algorithmType);
        System.out.println("Heuristic: " + heuristic.getName());
        System.out.println("Expanded boards: " + numOfExpandedBoards);
        if (openList != null && closedList != null) {//if algorithm is not AStar
            System.out.println("Openlist: " + openList.size());
            System.out.println("Closedlist: " + closedList.size());
        }
        if (algorithmType.equals("AStar")) {
            System.out.println("Depth: " + currentBoard.getGScore());
        } else {
            System.out.println("Depth: " + listBoards.getFirst().getGScore());
        }

        System.out.println("Memory used: " + memory + " KB");
        System.out.println("Run time: " + runtime);
        if (algorithmType.equals("AStar")) {
            System.out.println("Moves: " + Utils.getMoves(currentBoard));
        } else {
            System.out.println("Moves: " + Utils.getMoves(listBoards));
        }
        System.out.println("Board solved!");
        System.out.println("----------------------");
    }

    /**
     * Checks if the given board is valid
     *
     * @param board Board object
     */
    public static void checkInput(Board board) {
        if (board == null) {
            throw new PuzzleException("Given board is null");
        }
        if (board.getState() == null) {
            throw new PuzzleException("Given state is null");
        }
        if ((board.getState()[0].length | board.getState()[1].length | board.getState()[2].length | board.getState()[3].length) != 4 || board.getState().length != 4) {
            throw new PuzzleException("Puzzle has wrong dimensions");
        }
        if (!isSolvable(board.getState())) {
            throw new PuzzleException("Puzzle is not solvable");
        }
        int[] boardOneDim = arrayToOneDimension(board.getState());
        int[] goalOneDim = arrayToOneDimension(goal);
        Integer[] boxedBoard = Arrays.stream(boardOneDim).boxed().toArray(Integer[]::new);
        Integer[] boxedGoal = Arrays.stream(goalOneDim).boxed().toArray(Integer[]::new);
        HashSet<Integer> setBoard = new HashSet<>(Arrays.asList(boxedBoard));
        HashSet<Integer> setGoal = new HashSet<>(Arrays.asList(boxedGoal));
        if (!setBoard.equals(setGoal)) {
            throw new PuzzleException("Puzzle is not valid");
        }
    }

    /**
     * Converts a given String into an 2D array representing a solvable puzzle
     *
     * @param input "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0" OR
     *              "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0" OR
     *              "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 0"
     * @return 2D puzzle array
     */
    public static int[][] convertStringTo2DIntArray(String input) {
        String[] puzzle = input.split("[,\\s]+");

        if (puzzle.length != 16) {
            throw new PuzzleException("Invalid puzzle");
        }

        Set<Integer> set = new HashSet<>();
        int[][] array = new int[4][4];
        int index = 0;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int value = Integer.parseInt(puzzle[index++]);
                if (value < 0 || value > 15) {
                    throw new PuzzleException("Digit is not in range 0-15: " + value);
                }
                if (!set.add(value)) {
                    throw new PuzzleException("Puzzle has duplicates: " + value);
                }
                array[i][j] = value;
            }
        }

        return array;
    }


}
