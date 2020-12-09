package castle.comp3021.assignment.action;

import castle.comp3021.assignment.player.ComputerPlayer;
import castle.comp3021.assignment.player.ConsolePlayer;
import castle.comp3021.assignment.protocol.Action;
import castle.comp3021.assignment.protocol.Game;
import castle.comp3021.assignment.protocol.Piece;
import castle.comp3021.assignment.protocol.Place;
import castle.comp3021.assignment.protocol.exception.ActionException;

/**
 * Resume a paused piece.
 * <p>
 * The piece must belong to {@link ComputerPlayer}.
 * The piece must not be terminated.
 */
public class ResumePieceAction extends Action {

    /**
     * @param game the current {@link Game} object
     * @param args the arguments input by users in the console
     */
    public ResumePieceAction(Game game, String[] args) {
        super(game, args);
    }

    /**
     * Resume the piece according to {@link this#args}
     * Expected {@link this#args}: "a1"
     * Hint:
     * Consider corner cases (e.g., invalid {@link this#args})
     * Throw {@link ActionException} when exception happens.
     * <p>
     * Related meethods:
     * - {@link Piece#resume()}
     */
    @Override
    public void perform() throws ActionException {
        //TODO
        //no piece input
        if (this.args.length <= 0) {
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
        targetPiece.resume();
    }

    @Override
    public String toString() {
        return "Action[Resume piece]";
    }
}
