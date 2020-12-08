package castle.comp3021.assignment.action;

import castle.comp3021.assignment.player.ComputerPlayer;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.Action;
import castle.comp3021.assignment.protocol.Game;
import castle.comp3021.assignment.protocol.Piece;
import castle.comp3021.assignment.protocol.Place;
import castle.comp3021.assignment.protocol.exception.ActionException;

/**
 * The action to terminate a piece.
 * <p>
 * The piece must belong to {@link ComputerPlayer}.
 * After terminated, the piece cannot be paused or resumed.
 */
public class TerminatePieceAction extends Action {
    /**
     * @param game the current {@link Game} object
     * @param args the arguments input by users in the console
     */
    public TerminatePieceAction(Game game, String[] args) {
        super(game, args);
    }

    /**
     * Terminate the piece according to {@link this#args}
     * Expected {@link this#args}: "a1"
     * Hint:
     * Consider corner cases (e.g., invalid {@link this#args})
     * Throw {@link ActionException} when exception happens.
     * <p>
     * Related meethods:
     * - {@link Piece#terminate()}
     * - {@link Thread#interrupt()}
     * <p>
     * - The piece thread can be get by
     * {@link castle.comp3021.assignment.protocol.Configuration#getPieceThread(Piece)}
     */
    @Override
    public void perform() throws ActionException {
        //TODO
        //no piece input
        if (args.length <= 0){
            throw new ActionException("No piece provided");
        }
        //incorrect place input format
        Place targetPlace = ConsolePlayer.parsePlace(this.args[0]);
        if (targetPlace == null) {
            throw new ActionException("Invalid place input " + this.args[0]);
        }
        //no piece at place input
        Piece targetPiece = game.getPiece(targetPlace);
        if (targetPiece == null){
            throw new ActionException("piece does not exist at " + targetPlace.toString());
        }
        //non-computer piece
        if (!(targetPiece.getPlayer() instanceof ComputerPlayer)){
            throw new ActionException("piece at "
                    + targetPlace.toString()
                    + " does not belong to computer player, thus can not be paused");
        }
        //correctly getting computer piece
        Thread targetThread = game.getConfiguration().getPieceThread(targetPiece);
        targetPiece.terminate();
        targetThread.interrupt();
    }

    @Override
    public String toString() {
        return "Action[Terminate piece]";
    }
}