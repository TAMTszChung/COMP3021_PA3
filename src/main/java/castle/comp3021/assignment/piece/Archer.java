package castle.comp3021.assignment.piece;

import castle.comp3021.assignment.protocol.*;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Archer piece that moves similar to cannon in chinese chess.
 * Rules of move of Archer can be found in wikipedia (https://en.wikipedia.org/wiki/Xiangqi#Cannon).
 *
 * @see <a href='https://en.wikipedia.org/wiki/Xiangqi#Cannon'>Wikipedia</a>
 */
public class Archer extends Piece {
    static class InvalidMove extends Move {
        public InvalidMove() {
            super(-1, -1, -1, -1);
        }
    }

    /**
     * A BlockingDeque containing the candidate move
     */
    private final BlockingDeque<Move> candidateMoveQueue;

    /**
     * A LinkedBlockingDeque storing the parameters {@link Game} and {@link Place}
     * When calculateMoveParametersQueue is empty, the current piece thread should be waiting
     * until parameters {@link Game} and {@link Place} are passed in, the thread starts calculate the candidate move.
     */
    private final BlockingDeque<Object[]> calculateMoveParametersQueue;

    public Archer(Player player, Behavior behavior) {
        super(player, behavior);
        this.candidateMoveQueue = new LinkedBlockingDeque<>();
        this.calculateMoveParametersQueue = new LinkedBlockingDeque<>();
    }

    public Archer(Player player) {
        super(player);
        this.candidateMoveQueue = new LinkedBlockingDeque<>();
        this.calculateMoveParametersQueue = new LinkedBlockingDeque<>();
    }

    @Override
    public char getLabel() {
        return 'A';
    }

    @Override
    public Move[] getAvailableMoves(Game game, Place source) {
        var moves = new ArrayList<Move>();
        for (int x = 0; x < game.getConfiguration().getSize(); x++) {
            if (x != source.x()) {
                moves.add(new Move(source, x, source.y()));
            }
        }
        for (int y = 0; y < game.getConfiguration().getSize(); y++) {
            if (y != source.y()) {
                moves.add(new Move(source, source.x(), y));
            }
        }

        return moves.stream()
                .filter(move -> validateMove(game, move))
                .toArray(Move[]::new);
    }

    /**
     * Returns a valid candidate move given the current game {@link Game} and place  {@link Place} of the piece.
     * A 1 second timeout should be set.
     * If time is out, then no candidate move is proposed for this piece this round
     * The implementation is the same as {@link Knight#getCandidateMove(Game, Place)}
     * <p>
     * Hint:
     *      - The actual candidate move is selected in {@link Archer#run}
     *      - if the returned move is invalid, nothing should be returned.
     *      - Handle {@link InterruptedException}:
     *      - nothing should be returned in such case
     * @param game   the game object
     * @param source the current place of the piece
     * @return one candidate move
     */
    @Override
    public synchronized Move getCandidateMove(Game game, Place source) {
        //TODO
        Object[] parameters = {game, source};
        //clear both queue to prevent getting wrong move due to previous data
        calculateMoveParametersQueue.clear();
        candidateMoveQueue.clear();
        try {
            //put new entry
            this.calculateMoveParametersQueue.put(parameters);
            Move candidate = this.candidateMoveQueue.poll(1, TimeUnit.SECONDS);
            if (candidate instanceof Archer.InvalidMove){
                return null;
            }
            return candidate;
        } catch (InterruptedException e) {
            return null;
        }
    }

    private boolean validateMove(Game game, Move move) {
        var rules = new Rule[]{
                new OutOfBoundaryRule(),
                new OccupiedRule(),
                new VacantRule(),
                new NilMoveRule(),
                new FirstNMovesProtectionRule(game.getConfiguration().getNumMovesProtection()),
                new ArcherMoveRule(),
                new CriticalRegionRule(),
        };
        for (var rule :
                rules) {
            if (!rule.validate(game, move)) {
                return false;
            }
        }
        return true;
    }

    /**
     * An atomic boolean variable which marks whether this piece thread is running
     * running = true: this piece is running.
     * running = false: this piece is paused.
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * An atomic boolean variable which marks whether this piece thread is stopped
     * stopped = false: this piece is running.
     * stopped = true: this piece stops, and cannot be paused again.
     */
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    /**
     * Pause this piece thread.
     * Hint:
     * - Using {@link Archer#running}
     */
    @Override
    public void pause() {
        //TODO
        this.running.set(false);
    }

    /**
     * Resume the piece thread
     * Hint:
     * - Using {@link Archer#running}
     * - Using {@link Object#notifyAll()}
     */
    @Override
    public void resume() {
        //TODO
        this.running.set(true);
        synchronized (this.running){
            this.running.notifyAll();
        }
    }

    /**
     * Stop the piece thread
     * Hint:
     * - Using {@link Archer#stopped}
     * - Please do NOT use the deprecated {@link Thread#stop}
     */
    @Override
    public void terminate() {
        //TODO
        this.stopped.set(true);
    }

    /**
     * The piece should be runnable
     * Consider the following situations:
     *      - When it is NOT the turn of the player which this piece belongs to:
     *          - this thread should be waiting ({@link Object#wait()})
     *      - When it is the turn of the player which this piece belongs to (marked by {@link Archer#running}):
     *          - take out the {@link Game} and {@link Place} objects from calculateMoveParametersQueue
     *          - propose a candidate move (you may take advantage of {@link Archer#getAvailableMoves}
     *            using {@link MakeMoveByBehavior#getNextMove()} according to {@link this#behavior}
     *          - add to {@link Archer#candidateMoveQueue}
     *      - When this piece has been stopped (marked by {@link Archer#stopped}): no more reaction
     *      - Handle {@link InterruptedException}
     */
    @Override
    public void run() {
        //TODO
        do{
            try{
                //interrupted due to terminate
                if (this.stopped.get()){
                    return;
                }
                //interrupted due to pause
                synchronized (this.running){
                    while (!this.running.get()){
                        this.running.wait();
                    }
                }

                //get the candidate move
                Object[] parameters = this.calculateMoveParametersQueue.take();
                Game game = (Game) parameters[0];
                Place place = (Place) parameters[1];
                Move[] moves = this.getAvailableMoves(game, place);

                if (moves.length <=0){
                    this.candidateMoveQueue.put(new Archer.InvalidMove());
                    continue;
                }

                //put the candidate move to queue to be polled
                MakeMoveByBehavior moveByBehavior = new MakeMoveByBehavior(game, moves, this.behavior);
                Move nextMove = moveByBehavior.getNextMove();
                this.candidateMoveQueue.put(nextMove);
            } catch (InterruptedException e) {
                //go to next iteration to handle the interrupt in try block
            }
        } while (true);
    }
}
