package castle.comp3021.assignment.protocol;

import castle.comp3021.assignment.piece.Knight;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class MakeMoveByBehavior {
    private final Behavior behavior;
    private final Game game;
    private final Move[] availableMoves;

    public MakeMoveByBehavior(Game game, Move[] availableMoves, Behavior behavior){
        this.game = game;
        this.availableMoves = availableMoves;
        this.behavior = behavior;
    }

    /**
     * Return next move according to different strategies made by each piece.
     * You can add helper method if needed, as long as this method returns a next move.
     * - {@link Behavior#RANDOM}: return a random move from {@link this#availableMoves}
     * - {@link Behavior#GREEDY}: prefer the moves towards central place, the closer, the better
     * - {@link Behavior#CAPTURING}: prefer the moves that captures the enemies, killing the more, the better.
     *                               when there are many pieces that can captures, randomly select one of them
     * - {@link Behavior#BLOCKING}: prefer the moves that block enemy's {@link Knight}.
     *                              See how to block a knight here: https://en.wikipedia.org/wiki/Xiangqi (see `Horse`)
     *
     * @return a selected move adopting strategy specified by {@link this#behavior}
     */
    public Move getNextMove(){
        // TODO
        if (availableMoves.length <= 0){
            return null;
        }
        Move nextMove;
        switch (behavior){
            case GREEDY -> {
                nextMove =
                        Arrays.stream(availableMoves)
                        .filter(move -> {
                            if (move == null){
                                return false;
                            }
                            return getCenterDistance(move.getDestination()) < getCenterDistance(move.getSource());
                        })
                        .min(Comparator.comparing(move -> getCenterDistance(move.getDestination())))
                        .orElseGet(() -> availableMoves[new Random().nextInt(availableMoves.length)]);
            }
            case CAPTURING -> {
                Move[] capturingMoves =
                        Arrays.stream(availableMoves)
                                .filter(move -> {
                                    if (move == null){
                                        return false;
                                    }
                                    Piece destPiece = game.getPiece(move.getDestination());

                                    return ((destPiece != null)
                                            && !(destPiece.getPlayer().equals(game.currentPlayer)));
                                })
                                .collect(Collectors.toList()).toArray(Move[]::new);

                if (capturingMoves.length <= 0){
                    nextMove = availableMoves[new Random().nextInt(availableMoves.length)];
                }else{
                    nextMove = capturingMoves[new Random().nextInt(capturingMoves.length)];
                }
            }
            case BLOCKING -> {
                Move[] blockingMoves =
                        Arrays.stream(availableMoves)
                                .filter(move -> {
                                    if (move == null){
                                        return false;
                                    }
                                    Place dest = move.getDestination();
                                    int[] offsetX = {-1, 1, 0, 0};
                                    int[] offsetY = {0, 0, -1, 1};
                                    for (int i=0; i<offsetX.length; i++){
                                        Piece adjPiece = game.getPiece(dest.x()+offsetX[i], dest.y()+offsetY[i]);
                                        if (adjPiece !=null){
                                            if (adjPiece instanceof Knight &&
                                                    !(adjPiece.getPlayer().equals(game.currentPlayer))){
                                                return true;
                                            }
                                        }
                                    }
                                    return false;
                                })
                                .collect(Collectors.toList()).toArray(Move[]::new);

                if (blockingMoves.length <= 0){
                    nextMove = availableMoves[new Random().nextInt(availableMoves.length)];
                }else{
                    nextMove = blockingMoves[new Random().nextInt(blockingMoves.length)];
                }
            }
            default -> {
                nextMove = availableMoves[new Random().nextInt(availableMoves.length)];
            }
        }
        return nextMove;
    }

    private int getCenterDistance(Place place){
        if (place == null){
            return 99999999;
        }
        Place center = game.getCentralPlace();
        int xDist = Math.abs(place.x() - center.x());
        int yDist = Math.abs(place.y() - center.y());
        return Math.abs(xDist + yDist);
    }
}

