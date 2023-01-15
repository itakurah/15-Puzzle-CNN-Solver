package solver;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for representing the puzzle board
 */
public class Board {
    private final int COLUMN = 4;
    private final int ROW = 4;
    private int[][] state;
    private int fScore;
    private int gScore = 0;
    private int hScore;
    private int[] blankTile;
    private Board parent;
    private String move;
    private HeuristicType heuristicType;
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
        this.gScore = board.getgScore() + 1;
        this.state = copyState(board);
        this.heuristicType = board.getType();
        this.parent = board;
    }

    /**
     * Copies state from given board and returns 2d array
     *
     * @param board Board object
     * @return Copied array of parent board
     */
    public static int[][] copyState(Board board) {
        int[][] copy = new int[board.getState().length][];
        for (int i = 0; i < board.getROW(); i++) {
            copy[i] = Arrays.copyOf(board.getState()[i], board.getState().length);
        }
        return copy;
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
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                if (this.state[i][j] == number) {
                    return new int[]{i, j};
                }
            }
        }
        throw new IllegalArgumentException("Couldn't find number in board");
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
            //moveBoardRight.printBoard();
            moveBoardRight.getState()[x][y] = moveBoardRight.getState()[x][y + 1];
            moveBoardRight.getState()[x][y + 1] = 0;
            moveBoardRight.setBlankTile(copyBlankTile(this));
            moveBoardRight.setBlankTile(x, y + 1);
            moveBoardRight.setMove("R");
            moveBoardRight.sethScore(HeuristicSelection.getHeuristicScore(moveBoardRight, this.heuristicType));
            moveBoardRight.setfScore(moveBoardRight.getgScore() + moveBoardRight.gethScore());
            successors.add(moveBoardRight);
        }
        if (y > 0) {
            Board moveBoardLeft = new Board(this);
            moveBoardLeft.getState()[x][y] = moveBoardLeft.getState()[x][y - 1];
            moveBoardLeft.getState()[x][y - 1] = 0;
            moveBoardLeft.setBlankTile(copyBlankTile(this));
            moveBoardLeft.setBlankTile(x, y - 1);
            moveBoardLeft.setMove("L");
            moveBoardLeft.sethScore(HeuristicSelection.getHeuristicScore(moveBoardLeft, this.heuristicType));
            moveBoardLeft.setfScore(moveBoardLeft.getgScore() + moveBoardLeft.gethScore());
            successors.add(moveBoardLeft);
        }
        if (x > 0) {
            Board moveBoardUp = new Board(this);
            moveBoardUp.getState()[x][y] = moveBoardUp.getState()[x - 1][y];
            moveBoardUp.getState()[x - 1][y] = 0;
            moveBoardUp.setBlankTile(copyBlankTile(this));
            moveBoardUp.setBlankTile(x - 1, y);
            moveBoardUp.setMove("U");
            moveBoardUp.sethScore(HeuristicSelection.getHeuristicScore(moveBoardUp, this.heuristicType));
            moveBoardUp.setfScore(moveBoardUp.getgScore() + moveBoardUp.gethScore());
            successors.add(moveBoardUp);
        }
        if (x < 3) {
            Board moveBoardDown = new Board(this);
            moveBoardDown.getState()[x][y] = moveBoardDown.getState()[x + 1][y];
            moveBoardDown.getState()[x + 1][y] = 0;
            moveBoardDown.setBlankTile(copyBlankTile(this));
            moveBoardDown.setBlankTile(x + 1, y);
            moveBoardDown.setMove("D");
            moveBoardDown.sethScore(HeuristicSelection.getHeuristicScore(moveBoardDown, this.heuristicType));
            moveBoardDown.setfScore(moveBoardDown.getgScore() + moveBoardDown.gethScore());
            successors.add(moveBoardDown);
        }
        return successors;
    }

    /**
     * Prints the current board as a 4 by 4 matrix
     */
    public void printBoard() {
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
                System.out.printf("%4d", this.getState()[i][j]);
            }
            System.out.println();
        }
        System.out.println("--------------");
    }

    /**
     * Used for display current board object
     *
     * @return Board object details
     */
    @NonNull
    @Override
    public String toString() {
        return "Board{" +
                "state=" + Arrays.deepToString(getState()) +
                ", fScore=" + fScore +
                ", gScore=" + gScore +
                ", hScore=" + hScore +
                ", COLUMN=" + COLUMN +
                ", ROW=" + ROW +
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
     * Set f score
     *
     * @param fScore F score
     */
    public void setfScore(int fScore) {
        this.fScore = fScore;
    }

    /**
     * Get current g score
     *
     * @return G score
     */
    public int getgScore() {
        return gScore;
    }


    /**
     * Get current h score
     *
     * @return H score
     */
    public int gethScore() {
        return hScore;
    }

    /**
     * Set h score
     *
     * @param hScore H score
     */
    public void sethScore(int hScore) {
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
     * Get column size
     *
     * @return Column size
     */
    public int getCOLUMN() {
        return COLUMN;
    }

    /**
     * Get row size
     *
     * @return Row size
     */
    public int getROW() {
        return ROW;
    }


    /**
     * Get heuristic type
     *
     * @return Heuristic type
     */
    public HeuristicType getType() {
        return heuristicType;
    }

    /**
     * Set heuristic type
     *
     * @param type Type of heuristic
     */
    public void setType(HeuristicType type) {
        this.heuristicType = type;
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
