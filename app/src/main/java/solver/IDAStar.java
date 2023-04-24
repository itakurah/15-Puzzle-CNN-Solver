package solver;

import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * Class for the IDAStar algorithm
 */
public abstract class IDAStar {
    private static final long KILOBYTES = 1024L;
    private static final LinkedList<Board> path = new LinkedList<>();
    private static int numOfExpandedBoards = 0;

    /**
     * Solves a valid given 15-puzzle using IDAStar algorithm
     *
     * @param initialBoard Start board to solve
     * @param timeUnit     Unit for runtime (MS = milliseconds, NS = nanoseconds)
     * @param debugMode    Print results to console (ON = print information, OFF = print no information)
     * @param heuristic    Type of heuristic
     * @return Result object
     */
    public static Result solve(Board initialBoard, Heuristic heuristic, solver.TimeUnit timeUnit, DebugMode debugMode) {
        Utils.checkInput(initialBoard);
        Board BoardCopy = new Board(initialBoard.getState());//copy board
        BoardCopy.setHeuristic(heuristic);
        TimeUnit unit = null;
        switch (timeUnit) {
            case MS:
                unit = TimeUnit.MILLISECONDS;
            case NS:
                unit = TimeUnit.NANOSECONDS;
        }
        Stopwatch stopwatch = Stopwatch.createUnstarted();//create timer
        stopwatch.start();//start timer
        //clear up
        path.clear();
        numOfExpandedBoards = 0;
        //----------
        BoardCopy.setHScore(heuristic.calculate(BoardCopy));
        int threshold = BoardCopy.getHScore();
        path.addFirst(BoardCopy);
        while (true) {
            int t = search(0, threshold, heuristic);
            if (path.getFirst().isSolution()) {
                stopwatch.stop();
                Runtime runtime = Runtime.getRuntime();
                runtime.gc();
                long memory = runtime.totalMemory() - runtime.freeMemory();
                memory = memory / KILOBYTES;
                if (debugMode == DebugMode.ON) {
                    Utils.printResults("IDAStar", heuristic, numOfExpandedBoards,
                            null, null, null, path, memory, stopwatch.elapsed(unit));//print results
                }
                return new Result(path.getFirst(), "IDAStar", heuristic, numOfExpandedBoards,
                        0, 0, memory, stopwatch.elapsed(unit), Utils.getMoves(path));
            }
            threshold = t;
        }
    }

    /**
     * Performs a recursive depth-limited search
     *
     * @param gScore    Current G score
     * @param threshold Current threshold
     * @return Current threshold
     */
    private static int search(int gScore, int threshold, Heuristic heuristic) {
        int f = gScore + heuristic.calculate(path.getFirst());
        if (f > threshold) {
            return f;
        }
        if (path.getFirst().isSolution()) {
            return f;
        }
        int minF = Integer.MAX_VALUE;
        ArrayList<Board> successors = path.getFirst().generateSuccessors();
        numOfExpandedBoards += successors.size();
        for (Board successor : successors
        ) {
            if (!path.contains(successor)) {
                path.addFirst(successor);
                int t = search(successor.getGScore(), threshold, heuristic);
//                System.out.println(t);//display current f score
                if (path.getFirst().isSolution()) {
                    return t;
                }
                if (t < minF) {
                    minF = t;
                }
                path.pop();
            }
        }
        return minF;
    }
}
