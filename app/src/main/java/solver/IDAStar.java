package solver;

import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import static solver.TimeUnit.MS;

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
     * @param initialBoard  Start board to solve
     * @param timeUnit      Unit for runtime (MS = milliseconds, NS = nanoseconds)
     * @param debugMode     Print results to console (ON = print information, OFF = print no information)
     * @param heuristicType Type of heuristic (LCMD = Linear Conflicts with MD)
     * @return Result object
     */
    public static Result solve(Board initialBoard, HeuristicType heuristicType, solver.TimeUnit timeUnit, DebugMode debugMode) throws Exception {
        Utils.checkInput(initialBoard);
        Board BoardCopy = new Board(initialBoard.getState());//copy board
        TimeUnit unit;
        if(timeUnit==MS){
            unit = TimeUnit.MILLISECONDS;
        }else{
            unit = TimeUnit.NANOSECONDS;
        }
        Stopwatch stopwatch = Stopwatch.createUnstarted();//create timer
        stopwatch.start();//start timer
        BoardCopy.setType(heuristicType);
        //clear up
        path.clear();
        numOfExpandedBoards = 0;
        //----------
        BoardCopy.sethScore(HeuristicSelection.getHeuristicScore(BoardCopy, heuristicType));
        int threshold = BoardCopy.gethScore();
        path.addFirst(BoardCopy);
        while (true) {
            int t = search(0, threshold);
//            System.out.println(t);//display current f score
            if (Utils.isSolution(path.getFirst())) {
                stopwatch.stop();
                Runtime runtime = Runtime.getRuntime();
                runtime.gc();
                long memory = runtime.totalMemory() - runtime.freeMemory();
                memory = memory / KILOBYTES;
                if (debugMode == DebugMode.ON) {
                    Utils.printResults("IDAStar", heuristicType, numOfExpandedBoards,
                            path, memory, stopwatch.elapsed(unit));//print results
                }
                return new Result(path.getFirst(), "IDAStar", heuristicType, numOfExpandedBoards,
                        0, 0, memory, stopwatch.elapsed(unit), Utils.getMoves(path));
            }
            threshold = t;
        }
    }

    /**
     * Performs a recursive depth-limited search
     *
     * @param gScore    Curren G score
     * @param threshold Current threshold
     * @return Current threshold
     */
    private static int search(int gScore, int threshold) {
        int f = gScore + HeuristicSelection.getHeuristicScore(path.getFirst(), path.getFirst().getType());
        if (f > threshold) {
            return f;
        }
        if (Utils.isSolution(path.getFirst())) {
            return f;
        }
        int minF = Integer.MAX_VALUE;
        ArrayList<Board> successors = path.getFirst().generateSuccessors();
        numOfExpandedBoards += successors.size();
        for (Board successor : successors
        ) {
            if (!path.contains(successor)) {
                path.addFirst(successor);
                int t = search(successor.getgScore(), threshold);
//                System.out.println(t);//display current f score
                if (Utils.isSolution(path.getFirst())) {
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
