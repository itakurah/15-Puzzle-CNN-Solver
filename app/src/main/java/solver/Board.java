package solver;


import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for representing the puzzle board
 */
public class Board {
    public static final int[][] goal = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 0}};
    public static final int BOARD_LENGTH = 4;
    private static Heuristic heuristic;
    private final int[][] state;
    private int fScore;
    private int gScore = 0;
    private int hScore;
    private int[] blankTile;
    private Board parent;
    private String move;
    private ArrayList<Board> successors = new ArrayList<>();

    /**
     * Constructor for first board all successors uses copy constructor
     *
     * @param state State of the given board
     */
    public Board(int[][] state) {
        this.state = state;
        this.blankTile = getCoordinates(0);
    }

    /**
     * Copy constructor for successor boards
     *
     * @param board Board to create a copy from
     */
    public Board(Board board) {
        this.gScore = board.getGScore() + 1;
        this.state = copyState(board);
        heuristic = board.getHeuristic();
        this.parent = board;
    }

    /**
     * Copies state from given board and returns 2d array
     *
     * @param board Board object
     * @return Copied array of parent board
     */
    public static int[][] copyState(Board board) {
//        int[][] copy = new int[board.getState().length][];
//        for (int i = 0; i < board.getROW(); i++) {
//            copy[i] = Arrays.copyOf(board.getState()[i], board.getState().length);
//        }
        return Arrays.stream(board.getState()).map(int[]::clone).toArray(int[][]::new);//faster method due to Java stream Api;
    }

    /**
     * Get parent of current board
     *
     * @return Parent from current board
     */
    public Board getParent() {
        return parent;
    }

    /**
     * Set parent of current board
     *
     * @param parent Parent from given board
     */
    public void setParent(Board parent) {
        this.parent = parent;
    }

    /**
     * Get successors of current board
     *
     * @return List of successors
     */
    public ArrayList<Board> getSuccessors() {
        return successors;
    }

    /**
     * Set successors for current board
     *
     * @param successors List of successors
     */
    public void setSuccessors(ArrayList<Board> successors) {
        this.successors = successors;
    }

    /**
     * Get coordinates of given number
     *
     * @param number Number to get coordinates from
     * @return 2d array with x on first and y on second index
     */
    public int[] getCoordinates(int number) {
        for (int i = 0; i < BOARD_LENGTH; i++) {
            for (int j = 0; j < BOARD_LENGTH; j++) {
                if (this.state[i][j] == number) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalArgumentException("Number could not be found on the board");
    }

    /**
     * Copies the blank tile of parent board to the successor one
     *
     * @param board Board object
     * @return New Blank tile array
     */
    public int[] copyBlankTile(Board board) {
        int[] blankCoords = board.getBlankTile();
        return new int[]{blankCoords[0], blankCoords[1]};
    }

    /**
     * Generates successors of parent board
     *
     * @return List of successors
     */
    public ArrayList<Board> generateSuccessors() {
        ArrayList<Board> successors = new ArrayList<>();
        int x = this.getBlankTile()[0];
        int y = this.getBlankTile()[1];
        if (y < 3) {
            Board moveBoardRight = new Board(this);
            moveBoardRight.getState()[x][y] = moveBoardRight.getState()[x][y + 1];
            moveBoardRight.getState()[x][y + 1] = 0;
            moveBoardRight.setBlankTile(copyBlankTile(this));
            moveBoardRight.setBlankTile(x, y + 1);
            moveBoardRight.setMove("R");
            moveBoardRight.setHScore(heuristic.calculate(moveBoardRight));
            moveBoardRight.setFScore(moveBoardRight.getGScore() + moveBoardRight.getHScore());
            successors.add(moveBoardRight);
        }
        if (y > 0) {
            Board moveBoardLeft = new Board(this);
            moveBoardLeft.getState()[x][y] = moveBoardLeft.getState()[x][y - 1];
            moveBoardLeft.getState()[x][y - 1] = 0;
            moveBoardLeft.setBlankTile(copyBlankTile(this));
            moveBoardLeft.setBlankTile(x, y - 1);
            moveBoardLeft.setMove("L");
            moveBoardLeft.setHScore(heuristic.calculate(moveBoardLeft));
            moveBoardLeft.setFScore(moveBoardLeft.getGScore() + moveBoardLeft.getHScore());
            successors.add(moveBoardLeft);
        }
        if (x > 0) {
            Board moveBoardUp = new Board(this);
            moveBoardUp.getState()[x][y] = moveBoardUp.getState()[x - 1][y];
            moveBoardUp.getState()[x - 1][y] = 0;
            moveBoardUp.setBlankTile(copyBlankTile(this));
            moveBoardUp.setBlankTile(x - 1, y);
            moveBoardUp.setMove("U");
            moveBoardUp.setHScore(heuristic.calculate(moveBoardUp));
            moveBoardUp.setFScore(moveBoardUp.getGScore() + moveBoardUp.getHScore());
            successors.add(moveBoardUp);
        }
        if (x < 3) {
            Board moveBoardDown = new Board(this);
            moveBoardDown.getState()[x][y] = moveBoardDown.getState()[x + 1][y];
            moveBoardDown.getState()[x + 1][y] = 0;
            moveBoardDown.setBlankTile(copyBlankTile(this));
            moveBoardDown.setBlankTile(x + 1, y);
            moveBoardDown.setMove("D");
            moveBoardDown.setHScore(heuristic.calculate(moveBoardDown));
            moveBoardDown.setFScore(moveBoardDown.getGScore() + moveBoardDown.getHScore());
            successors.add(moveBoardDown);
        }
        return successors;
    }

    /**
     * Prints the current board as a 4 by 4 matrix
     */
    public void show() {
        System.out.println("+-----+-----+-----+-----+\n" +
                "| " + formatCell(this.getState()[0][0]) + " | " + formatCell(this.getState()[0][1]) + " | " + formatCell(this.getState()[0][2]) + " | " + formatCell(this.getState()[0][3]) + " |\n" +
                "+-----+-----+-----+-----+\n" +
                "| " + formatCell(this.getState()[1][0]) + " | " + formatCell(this.getState()[1][1]) + " | " + formatCell(this.getState()[1][2]) + " | " + formatCell(this.getState()[1][3]) + " |\n" +
                "+-----+-----+-----+-----+\n" +
                "| " + formatCell(this.getState()[2][0]) + " | " + formatCell(this.getState()[2][1]) + " | " + formatCell(this.getState()[2][2]) + " | " + formatCell(this.getState()[2][3]) + " |\n" +
                "+-----+-----+-----+-----+\n" +
                "| " + formatCell(this.getState()[3][0]) + " | " + formatCell(this.getState()[3][1]) + " | " + formatCell(this.getState()[3][2]) + " | " + formatCell(this.getState()[3][3]) + " |\n" +
                "+-----+-----+-----+-----+\n");
    }

    /**
     * Formats the cells in the grid
     * @param value
     * @return
     */
    private String formatCell(int value) {
        return (value == 0) ? "   " : ((value < 10) ? " " + value + " " : value + " ");
    }

    /**
     * Used for display current board object
     *
     * @return Board object details
     */
    @Override
    public String toString() {
        return "Board{" +
                "state=" + Arrays.deepToString(getState()) +
                ", fScore=" + fScore +
                ", gScore=" + gScore +
                ", hScore=" + hScore +
                ", COLUMN=" + BOARD_LENGTH +
                ", ROW=" + BOARD_LENGTH +
                '}';
    }

    /**
     * Get 2d array of current board
     *
     * @return 2D array
     */
    public int[][] getState() {
        return state;
    }

    /**
     * Get current f score
     *
     * @return F score
     */
    public int getFScore() {
        return fScore;
    }

    /**
     * Set f score
     *
     * @param fScore F score
     */
    public void setFScore(int fScore) {
        this.fScore = fScore;
    }

    /**
     * Get current g score
     *
     * @return G score
     */
    public int getGScore() {
        return gScore;
    }

    /**
     * Set g score
     *
     * @param gScore G score
     */
    public void setGScore(int gScore) {
        this.gScore = gScore;
    }

    /**
     * Get current h score
     *
     * @return H score
     */
    public int getHScore() {
        return hScore;
    }

    /**
     * Set h score
     *
     * @param hScore H score
     */
    public void setHScore(int hScore) {
        this.hScore = hScore;
    }

    /**
     * Get last move
     *
     * @return Last move
     */
    public String getMove() {
        return move;
    }

    /**
     * Set last move
     *
     * @param move Last move
     */
    public void setMove(String move) {
        this.move = move;
    }

    /**
     * Get heuristic type
     *
     * @return Heuristic type
     */
    public Heuristic getHeuristic() {
        return heuristic;
    }

    /**
     * Set heuristic type
     *
     * @param heuristic Type of heuristic
     */
    public void setHeuristic(Heuristic heuristic) {
        Board.heuristic = heuristic;
    }

    /**
     * Get blank tile
     *
     * @return Blank tile array
     */
    public int[] getBlankTile() {
        return blankTile;
    }

    /**
     * Set blank tile array
     *
     * @param blankTileCoords Coordinates of new blank tile
     */
    public void setBlankTile(int[] blankTileCoords) {
        this.blankTile = blankTileCoords;
    }

    /**
     * Set blank tile to given x and y coordinate
     *
     * @param x Coordinate
     * @param y Coordinate
     */
    public void setBlankTile(int x, int y) {
        this.blankTile[0] = x;
        this.blankTile[1] = y;
    }

    public boolean isSolution() {
        return this.equals(new Board(goal));
    }

    /**
     * Returns true if current board is equal to given board
     * determined using the 2d array of the boards
     *
     * @param o Given board object
     * @return true if both objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return Arrays.deepEquals(state, board.state);
    }

    /**
     * Used for comparison
     *
     * @return Hashcode of array
     */
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(state);
    }
}
