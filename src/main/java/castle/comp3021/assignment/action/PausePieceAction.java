package castle.comp3021.assignment.action;

import castle.comp3021.assignment.player.ComputerPlayer;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.Action;
import castle.comp3021.assignment.protocol.Game;
import castle.comp3021.assignment.protocol.Piece;
import castle.comp3021.assignment.protocol.Place;
import castle.comp3021.assignment.protocol.exception.ActionException;

/**
 * The action to pause a piece.
 * <p>
 * The piece must belong to {@link ComputerPlayer}.
 * The piece must not be terminated.
 */
public class PausePieceAction extends Action {
    /**
     * @param game the current {@link Game} object
     * @param args the arguments input by users in the console
     */
    public PausePieceAction(Game game, String[] args) {
        super(game, args);
    }

    /**
     * Pause the piece according to {@link this#args}
     * Expected {@link this#args}: "a1"
     * Hint:
     * Consider corner cases (e.g., invalid {@link this#args})
     * Throw {@link ActionException} when exception happens.
     * <p>
     * Related meethods:
     * - {@link Piece#pause()}
     * - {@link Thread#interrupt()}
     * <p>
     * - The piece thread can be get by
     * {@link castle.comp3021.assignment.protocol.Configuration#getPieceThread(Piece)}
     */
    @Override
    public void perform() throws ActionException {
        //TODO
        if (args.length <= 0){
            throw new ActionException("No piece provided");
        }
        Place targetPlace = ConsolePlayer.parsePlace(this.args[0]);
        if (targetPlace == null) {
            throw new ActionException("Invalid place input" + this.args[0]);
        }

        Piece targetPiece = game.getPiece(targetPlace);
        if (targetPiece == null){
            throw new ActionException("piece does not exist at " + targetPlace.toString());
        }
        if (!(targetPiece.getPlayer() instanceof ComputerPlayer)){
            throw new ActionException("piece at "
                    + targetPlace.toString()
                    + " does not belong to computer player, thus can not be paused");
        }
        Thread targetThread = game.getConfiguration().getPieceThread(targetPiece);
        targetPiece.pause();
        targetThread.interrupt();
    }

    @Override
    public String toString() {
        return "Action[Pause piece]";
    }
}