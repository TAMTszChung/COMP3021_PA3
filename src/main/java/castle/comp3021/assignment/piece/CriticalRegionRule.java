package castle.comp3021.assignment.piece;

import castle.comp3021.assignment.protocol.*;

public class CriticalRegionRule implements Rule {


    /**
     * Validate whether the proposed  move will violate the critical region rule
     * I.e., there are no more than {@link Configuration#getCriticalRegionCapacity()} in the critical region.
     * Determine whether the move is in critical region, using {@link this#isInCriticalRegion(Game, Place)}
     * @param game the current game object
     * @param move the move to be validated
     * @return whether the given move is valid or not
     */
    @Override
    public boolean validate(Game game, Move move) {
        //TODO
        Piece currentPiece = game.getPiece(move.getSource());
        if (currentPiece == null){
            return false;
        }

        if (!(currentPiece instanceof Knight)){
            return true;
        }

        if (isInCriticalRegion(game, move.getSource())){
            return true;
        }

        if (!isInCriticalRegion(game, move.getDestination())){
            return true;
        }

        int capacity = game.getConfiguration().getCriticalRegionCapacity();
        Player currentPlayer = currentPiece.getPlayer();
        int numCriticalKnight = 0;
        int boardSize = game.getConfiguration().getSize();
        for (int i=0; i < boardSize; i++){
            for (int j=0; j < boardSize; j++){
                Piece tempPiece = game.getPiece(i, j);
                if (tempPiece == null){
                    continue;
                }
                if (!tempPiece.getPlayer().equals(currentPlayer)){
                    continue;
                }
                if (!(tempPiece instanceof Knight)){
                    continue;
                }
                if (!isInCriticalRegion(game, new Place(i,j))){
                    continue;
                }
                numCriticalKnight++;
            }
        }

        return numCriticalKnight < capacity;
    }

    /**
     * Check whether the given move is in critical region
     * Critical region is {@link Configuration#getCriticalRegionSize()} of rows, centered around center place
     * Example:
     *      In a 5 * 5 board, which center place lies in the 3rd row
     *      Suppose critical region size = 3, then for row 1-5, the critical region include row 2-4.
     * @param game the current game object
     * @param place the move to be validated
     * @return whether the given move is in critical region
     */
    private boolean isInCriticalRegion(Game game, Place place) {
        //TODO
        int criticalRegionSize = game.getConfiguration().getCriticalRegionSize();
        int criticalRowsFromCenter = criticalRegionSize / 2;
        Place centerPlace = game.getCentralPlace();
        int centerY = centerPlace.y();
        int smallCritical = centerY - criticalRowsFromCenter;
        int largeCritical = centerY + criticalRowsFromCenter;
        int currentY = place.y();

        return currentY >= smallCritical && currentY <= largeCritical;
    }

    @Override
    public String getDescription() {
        return "critical region is full";
    }
}
