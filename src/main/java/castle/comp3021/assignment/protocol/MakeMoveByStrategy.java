package castle.comp3021.assignment.protocol;

import castle.comp3021.assignment.piece.Knight;

import java.util.*;
import java.util.stream.Collectors;

public class MakeMoveByStrategy {
    private final Strategy strategy;
    private final Game game;
    private final Move[] availableMoves;

    public MakeMoveByStrategy(Game game, Move[] availableMoves, Strategy strategy){
        this.game = game;
        this.availableMoves = availableMoves;
        this.strategy = strategy;
    }

    /**
     * Return next move according to different strategies made by {@link castle.comp3021.assignment.player.ComputerPlayer}
     * You can add helper method if needed, as long as this method returns a next move.
     * - {@link Strategy#RANDOM}: select a random move from the proposed moves by all pieces
     * - {@link Strategy#SMART}: come up with some strategy to select a next move from the proposed moves by all pieces
     *
     * @return a next move
     */
    public Move getNextMove(){
        // TODO
        if (availableMoves.length <= 0){
            return null;
        }

        if (strategy == Strategy.RANDOM){
            return availableMoves[new Random().nextInt(availableMoves.length)];
        }

        return chooseSmartMove();
    }

    private Move chooseSmartMove(){
        //if there is a move that will win
        Move winningMove = getWinningMove();
        if (winningMove != null){
            return winningMove;
        }

        //move Knight to center as much as possible
        Move mostGreedy = getBestGreedy();
        if (mostGreedy != null){
            return mostGreedy;
        }

        //if a move prevent enemy win
        Move criticalCapture = getCriticalCapture();
        if (criticalCapture != null){
            return criticalCapture;
        }

        Move bestCapture = getBestCapture();
        if (bestCapture != null){
            return bestCapture;
        }

        Move bestBlocking = getBestBlocking();
        if (bestBlocking != null){
            return bestBlocking;
        }

        Move minMove = Arrays.stream(availableMoves).min(Comparator.comparing(this::moveDist)).orElse(null);
        if (minMove != null){
            return minMove;
        }

        return availableMoves[new Random().nextInt(availableMoves.length)];
    }

    private Move getWinningMove(){
        List<Move> winningMoves
                = Arrays.stream(availableMoves)
                .filter(this::winningMoveCheck)
                .collect(Collectors.toList());
        if (!winningMoves.isEmpty()){
            return winningMoves.get(new Random().nextInt(winningMoves.size()));
        }else{
            return null;
        }
    }

    private boolean winningMoveCheck(Move move){
        if (move == null){
            return false;
        }
        if (game.getNumMoves() < game.getConfiguration().getNumMovesProtection()){
            return false;
        }
        if (game.getPiece(move.getSource()) instanceof Knight){
            if (move.getSource().equals(game.getCentralPlace())
                    && !move.getDestination().equals(game.getCentralPlace())){
                return true;
            }
        }

        Piece destPiece = game.getPiece(move.getDestination());
        if (destPiece == null){
            return false;
        }

        int enemyPieces = 0;
        int boardSize = game.getConfiguration().getSize();
        for (int i=0; i<boardSize; i++){
            for (int j=0; j<boardSize; j++){
                Piece tempPiece = game.getPiece(i,j);
                if (tempPiece == null || tempPiece.getPlayer().equals(game.currentPlayer)){
                    continue;
                }
                enemyPieces += 1;
            }
        }

        if (enemyPieces == 1){
            if (!destPiece.getPlayer().equals(game.currentPlayer)){
                return true;
            }
        }

        return false;
    }

    private Move getBestGreedy() {
        List<Move> knightGreedyMove = Arrays
                .stream(availableMoves)
                .filter(move -> game.getPiece(move.getSource()) instanceof Knight)
                .filter(this::greedyCheck)
                .collect(Collectors.toList());

        if (knightGreedyMove.isEmpty()){
            return null;
        }

        Move bestGreedy = knightGreedyMove.stream()
                .filter(this::easyCenterCheck)
                .min(Comparator.comparing(move -> getCenterDistance(move.getDestination())))
                .orElse(null);

        if (bestGreedy != null){
            return bestGreedy;
        }

        return knightGreedyMove.stream()
                .min(Comparator.comparing(move -> getCenterDistance(move.getDestination())))
                .orElse(null);
    }

    private boolean greedyCheck(Move move){
        if (move == null){
            return false;
        }
        return getCenterDistance(move.getDestination()) < getCenterDistance(move.getSource());
    }

    private boolean easyCenterCheck(Move move){
        if (move == null){
            return false;
        }
        return getCenterDistance(move.getDestination())%3 == 0;
    }

    private Move getCriticalCapture(){
        return Arrays.stream(availableMoves)
                .filter(this::criticalCaptureCheck)
                .min(Comparator.comparing(this::moveDist))
                .orElse(null);
    }

    private boolean criticalCaptureCheck(Move move){
        if (move == null){
            return false;
        }
        Place dest = move.getDestination();
        Piece destPiece = game.getPiece(dest);
        if (destPiece == null || destPiece.getPlayer().equals(game.currentPlayer)){
            return false;
        }

        int distCenterX = Math.abs(move.getDestination().x() - game.getCentralPlace().x());
        int distCenterY = Math.abs(move.getDestination().y() - game.getCentralPlace().y());

        if ((distCenterX==1 && distCenterY==2) || (distCenterX==2 && distCenterY==1)){
            if (!destPiece.getPlayer().equals(game.currentPlayer)){
                return true;
            }
        }

        return false;
    }

    private Move getBestCapture(){
        List<Move> captures = Arrays
                .stream(availableMoves)
                .filter(this::capturingCheck)
                .collect(Collectors.toList());
        if (captures.isEmpty()){
            return null;
        }

        Move bestCapture = captures.stream()
                .filter(this::captureKnightCheck)
                .min(Comparator.comparing(this::moveDist))
                .orElse(null);

        if (bestCapture != null){
            return bestCapture;
        }

        return captures.stream()
                .min(Comparator.comparing(this::moveDist))
                .orElse(null);
    }

    private boolean capturingCheck(Move move){
        if (move == null){
            return false;
        }
        Place dest = move.getDestination();
        Piece destPiece = game.getPiece(dest);
        if (destPiece == null || destPiece.getPlayer().equals(game.currentPlayer)){
            return false;
        }

        return true;
    }

    private boolean captureKnightCheck(Move move){
        if (move == null){
            return false;
        }
        Place dest = move.getDestination();
        Piece destPiece = game.getPiece(dest);
        if (destPiece == null || destPiece.getPlayer().equals(game.currentPlayer)){
            return false;
        }

        if (destPiece instanceof Knight){
            return true;
        }
        return false;
    }

    private Move getBestBlocking(){
        return Arrays.stream(availableMoves)
                .filter(this::blockingCheck)
                .min(Comparator.comparing(this::moveDist))
                .orElse(null);
    }

    private boolean blockingCheck(Move move){
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
    }

    private int moveDist(Move move){
        if (move == null
                || move.getSource() == null
                || move.getDestination() ==null){
            return 99999999;
        }
        int xDist = Math.abs(move.getSource().x() - move.getDestination().x());
        int yDist = Math.abs(move.getSource().y() - move.getDestination().y());
        return Math.abs(xDist + yDist);
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
