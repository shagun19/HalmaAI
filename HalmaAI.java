import java.util.*;

public class HalmaAI {
    private class Node {
        private char[][] currentInputMatrix;
        private char currentPlayer;
        private List<Integer> currentPlayerPosition;
        private List<List<List<Integer>>> nextPostions;

        public Node(char currentPlayer, List<Integer> currentPlayerPosition, char[][] currentInputMatrix,
                    List<List<List<Integer>>> nextPostions) {
            this.currentPlayer = currentPlayer;
            this.currentPlayerPosition = currentPlayerPosition;
            this.currentInputMatrix = currentInputMatrix;
            this.nextPostions = nextPostions;
        }

        public Node() {
            this.currentInputMatrix = new char[BOARD_SIZE][BOARD_SIZE];
            this.currentPlayerPosition = new ArrayList<>();
            this.nextPostions = new ArrayList<>();
            this.currentPlayer = NO_PLAYER;
        }
    }

    /*class NodeComparator implements Comparator<Node>{
        public int compare(Node node1, Node node2) {
            if (node1.nextPostions.get(0).get(0).get(2) >= node2.nextPostions.get(0).get(0).get(2))
                return -1;
            else return 1;
        }
    }*/


    private final int BOARD_SIZE = 16;
    private final int PAWN_COUNT = 19;
    private final char BLACK_PLAYER = 'B';
    private final char WHITE_PLAYER = 'W';
    private final char NO_PLAYER = '.';
    private final int[] adjacency_X = {-1, 0, 1, -1, 1, -1, 0, 1};
    private final int[] adjacency_Y = {1, 1, 1, 0, 0, -1, -1, -1};
    private final int[] nextAdjacency_X = {-2, 0, 2, -2, 2, -2, 0, 2};
    private final int[] nextAdjacency_Y = {2, 2, 2, 0, 0, -2, -2, -2};
    List<List<Integer>> oppositeCampWhite = Arrays.asList(Arrays.asList(0, 0), Arrays.asList(0, 1), Arrays.asList(0, 2),
            Arrays.asList(0, 3), Arrays.asList(0, 4), Arrays.asList(1, 0), Arrays.asList(1, 1), Arrays.asList(1, 2),
            Arrays.asList(1, 3), Arrays.asList(1, 4), Arrays.asList(2, 0), Arrays.asList(2, 1), Arrays.asList(2, 2),
            Arrays.asList(2, 3), Arrays.asList(3, 0), Arrays.asList(3, 1), Arrays.asList(3, 2), Arrays.asList(4, 0),
            Arrays.asList(4, 1));
    List<List<Integer>> oppositeCampBlack = Arrays.asList(Arrays.asList(15, 11), Arrays.asList(15, 12),
            Arrays.asList(15, 13), Arrays.asList(15, 14), Arrays.asList(15, 15), Arrays.asList(14, 11),
            Arrays.asList(14, 12), Arrays.asList(14, 13), Arrays.asList(14, 14), Arrays.asList(14, 15),
            Arrays.asList(13, 12), Arrays.asList(13, 13), Arrays.asList(13, 14), Arrays.asList(13, 15),
            Arrays.asList(12, 13), Arrays.asList(12, 14), Arrays.asList(12, 15), Arrays.asList(11, 14),
            Arrays.asList(11, 15));
    List<List<Integer>> whiteCampEntryPoints = Arrays.asList(Arrays.asList(10, 15), Arrays.asList(10, 14),
            Arrays.asList(11, 13), Arrays.asList(12, 12), Arrays.asList(13, 11), Arrays.asList(14, 10), Arrays.asList(15, 10));
    List<List<Integer>> blackCampEntryPoints = Arrays.asList(Arrays.asList(0, 5), Arrays.asList(1, 5),
            Arrays.asList(2, 4), Arrays.asList(3, 3), Arrays.asList(4, 2), Arrays.asList(5, 0), Arrays.asList(5, 1));

    private List<List<Integer>> getPawnPositions(char[][] inputMatrix, char player) {
        // We are ensuring that we first consider only those pawns which are within their camp
        List<List<Integer>> positions = new ArrayList<>(PAWN_COUNT);
        if (player == BLACK_PLAYER) {
            for (List<Integer> position : oppositeCampWhite) {
                if (inputMatrix[position.get(0)][position.get(1)] == BLACK_PLAYER) positions.add(position);
            }
        } else {
            for (List<Integer> position : oppositeCampBlack) {
                if (inputMatrix[position.get(0)][position.get(1)] == WHITE_PLAYER) positions.add(position);
            }
        }
        if (positions.size() == 0) {
            for (int i = 0; i < inputMatrix.length; i++) {
                for (int j = 0; j < inputMatrix.length; j++) {
                    if (inputMatrix[i][j] == player) positions.add(Arrays.asList(i, j));
                }
            }
        }
        return positions;
    }

    /* This ensures that once you enter opposite camp you cannot leave it. Or if you are not in the opposite
     camp, then you can obviously land anywhere (in or outside the camp).
     Also you land on valid cell of a board */
    private boolean checkInitialValidity(List<Integer> currentPosition, List<Integer> newPosition, char player) {
        boolean valid = newPosition.get(0) >= 0 && newPosition.get(0) < BOARD_SIZE &&
                newPosition.get(1) >= 0 && newPosition.get(1) < BOARD_SIZE;
        if (player == BLACK_PLAYER && !(oppositeCampBlack.contains(currentPosition) &&
                !oppositeCampBlack.contains(newPosition))) {
            return valid;
        } else if (player == WHITE_PLAYER && !(oppositeCampWhite.contains(currentPosition) &&
                !oppositeCampWhite.contains(newPosition))) {
            return valid;
        }
        return false;
    }

    private void getValidPositions(char[][] inputMatrix, List<Integer> currentPosition, char player,
                                   List<List<Integer>> validPositions, List<Integer> oldPosition) {
        int currentPositionX = currentPosition.get(0);
        int currentPositionY = currentPosition.get(1);
        List<Integer> newPosition;
        for (int i = 0; i < adjacency_X.length; i++) {
            if (oldPosition.isEmpty()) newPosition = Arrays.asList(currentPositionX + adjacency_X[i],
                    currentPositionY + adjacency_Y[i]);
            else newPosition = Arrays.asList(currentPositionX + nextAdjacency_X[i],
                    currentPositionY + nextAdjacency_Y[i]);
            boolean result = checkInitialValidity(currentPosition, newPosition, player);
            if (result) {
                if (oldPosition.isEmpty() && inputMatrix[newPosition.get(0)][newPosition.get(1)] == NO_PLAYER) {
                    if (!validPositions.contains(newPosition)) validPositions.add(newPosition); // No Jump
                    else return;
                } else {
                    List<Integer> nextNewPosition = Arrays.asList(currentPositionX + nextAdjacency_X[i],
                            currentPositionY + nextAdjacency_Y[i]);
                    if (checkInitialValidity(currentPosition, nextNewPosition, player)) {
                        if (!oldPosition.isEmpty()) { // Landed here through previous hop
                            if (nextNewPosition != oldPosition && inputMatrix[currentPosition.get(0) +
                                    adjacency_X[i]][currentPosition.get(1) + adjacency_Y[i]] != NO_PLAYER &&
                                    inputMatrix[nextNewPosition.get(0)][nextNewPosition.get(1)] == NO_PLAYER) {
                                if (!validPositions.contains(nextNewPosition)) validPositions.add(nextNewPosition);
                                else return;
                                getValidPositions(inputMatrix, nextNewPosition, player, validPositions, currentPosition);
                            }
                        } else if (inputMatrix[nextNewPosition.get(0)][nextNewPosition.get(1)] == NO_PLAYER) { // first ever
                            // hop  lands here
                            if (!validPositions.contains(nextNewPosition)) validPositions.add(nextNewPosition);
                            else return;
                            getValidPositions(inputMatrix, nextNewPosition, player, validPositions, currentPosition);

                        }
                    }
                }
            }
        }
    }

    /* Utility
    1. Current player positions + opposite player positions = opposite camp completely occupied
    2. Check getting opposite player move could lead to your loss
    3. Check if you move farthest in your move ; might add diagonal zone
    4.

    */
    private List<List<Integer>> getUtilityOfNextPosition(char player, char[][] inputMatrix, List<List<Integer>> validPositions) {
        List<List<Integer>> oppositeCamp;
        List<List<Integer>> entryPoints;
        List<List<Integer>> validPositionsWithUtility = new ArrayList<>();
        if (player == BLACK_PLAYER) {
            oppositeCamp = new ArrayList<>(oppositeCampBlack);
            entryPoints = new ArrayList<>(whiteCampEntryPoints);
        } else {
            oppositeCamp = new ArrayList<>(oppositeCampWhite);
            entryPoints = new ArrayList<>(blackCampEntryPoints);
        }
        for (List<Integer> position : validPositions) {
            int count = 0;
            int utilityValue;
            if (oppositeCamp.contains(position)) {
                for (List<Integer> campPosition : oppositeCamp) {
                    if (inputMatrix[campPosition.get(0)][campPosition.get(1)] != NO_PLAYER) ++count;
                }
                if (count == 18) {
                    utilityValue = 5000;
                } else utilityValue = 3000 - (18 - count);
            } else {
                List<Integer> proximityValues = new ArrayList<>();
                for (List<Integer> entryPoint : entryPoints) {
                    proximityValues.add((int) (10.0 * Math.sqrt(Math.pow((entryPoint.get(0) - position.get(0)), 2) +
                            Math.pow((entryPoint.get(1) - position.get(1)), 2))));
                }
                if (Collections.min(proximityValues) != 0) utilityValue = 20000 / Collections.min(proximityValues);
                else utilityValue = 2500;
            }
            validPositionsWithUtility.add(Arrays.asList(position.get(0), position.get(1), utilityValue / 50));
        }
        validPositions.clear();
        return validPositionsWithUtility;
    }

    /* These two checks done in filter function below:
    1. Need to ensure now that once you leave your camp, you cannot enter back
    2. If you cannot leave your camp; try getting away from your corner */
    private List<List<Integer>> filterCampPositions(char player, List<Integer> currentPosition,
                                                    List<List<Integer>> validPositionsObtained) {
        List<List<Integer>> playerCamp;
        if (player == BLACK_PLAYER) playerCamp = new ArrayList<>(oppositeCampWhite);
        else playerCamp = new ArrayList<>(oppositeCampBlack);
        List<List<Integer>> validPositionsModified = new ArrayList<>(validPositionsObtained);
        if (playerCamp.contains(currentPosition)) {
            for (List<Integer> position : validPositionsObtained) {
                if (playerCamp.contains(position)) validPositionsModified.remove(position);
            }
            if (validPositionsModified.isEmpty()) {
                validPositionsModified = new ArrayList<>(validPositionsObtained);
                for (List<Integer> position : validPositionsObtained) {
                    if (player == BLACK_PLAYER && (position.get(0) < currentPosition.get(0) || position.get(1) < currentPosition.get(1)))
                        validPositionsModified.remove(position);
                    else if (player == WHITE_PLAYER && (position.get(0) > currentPosition.get(0) || position.get(1) > currentPosition.get(1)))
                       validPositionsModified.remove(position);
                }
            }
        }
        return validPositionsModified;
    }

    private List<List<List<Integer>>> getHops(char[][] inputMatrix, char player) {
        List<List<Integer>> currentPositions = getPawnPositions(inputMatrix, player);
        List<List<List<Integer>>> nextValidPosition = new ArrayList<>(PAWN_COUNT);
        for (List<Integer> currentPosition : currentPositions) {
            List<List<Integer>> validPositions = new ArrayList<>();
            getValidPositions(inputMatrix, currentPosition, player, validPositions, new ArrayList<>());
            validPositions=filterCampPositions(player, currentPosition, validPositions);
            validPositions = getUtilityOfNextPosition(player, inputMatrix, validPositions);
            validPositions.sort((o1, o2) -> o2.get(2).compareTo(o1.get(2)));
            nextValidPosition.add(validPositions);
        }
        return nextValidPosition;
    }

    private int getSizeOfNextValidPosition(List<List<List<Integer>>> playerNextValidPositions) {
        int count = 0;
        for (int i = 0; i < playerNextValidPositions.size(); i++) {
            for (int j = 0; j < playerNextValidPositions.get(i).size(); j++) ++count;
        }
        return count;
    }

    private List<Node> generateGameTree(char[][] inputMatrix, char firstPlayer,
                                        List<List<Integer>> firstPlayerCurrentPositions,
                                        List<List<List<Integer>>> firstPlayerNextValidPositions, List<Node> nodes) {
        char secondPlayer;
        if (firstPlayer == BLACK_PLAYER) secondPlayer = WHITE_PLAYER;
        else secondPlayer = BLACK_PLAYER;
        for (int i = 0; i < firstPlayerCurrentPositions.size(); i++) {
            for (int j = 0; j < firstPlayerNextValidPositions.get(i).size(); j++) { // 40 times
                char[][] inputMatrixDup = Arrays.stream(inputMatrix).map(char[]::clone).toArray(char[][]::new);
                inputMatrixDup[firstPlayerCurrentPositions.get(i).get(0)]
                        [firstPlayerCurrentPositions.get(i).get(1)] = NO_PLAYER;
                inputMatrixDup[firstPlayerNextValidPositions.get(i).get(j).get(0)]
                        [firstPlayerNextValidPositions.get(i).get(j).get(1)] = firstPlayer;
                List<List<List<Integer>>> secondPlayerNextValidPositions = getHops(inputMatrixDup, secondPlayer);
                Node node = new Node(secondPlayer, firstPlayerNextValidPositions.get(i).get(j).subList(0, 2),
                        inputMatrixDup, secondPlayerNextValidPositions);
                nodes.add(node);
            }
        }
        return nodes;
    }

    private List<Node> generateGameTreeAtDepth(char firstPlayer, int depth, char[][] inputMatrix) {
        List<Node> nodesCurrent = new ArrayList<>();
        List<Node> nodesNext = new ArrayList<>();
        List<List<Integer>> pawnPositions = getPawnPositions(inputMatrix, firstPlayer);
        List<List<List<Integer>>> nextPositions = getHops(inputMatrix, firstPlayer);
        /*-------
        for(int i=0;i<nextPositions.get(0).size();i++){
            Node temp = new Node(firstPlayer,pawnPositions.get(0),inputMatrix,
                    Arrays.asList(Arrays.asList(nextPositions.get(0).get(i))));
            nodesCurrent.add(temp);
        }
        if (depth == 1) return nodesCurrent;
        //-------*/
        generateGameTree(inputMatrix, firstPlayer, pawnPositions, nextPositions, nodesCurrent);
        if (depth == 1) return nodesCurrent;
        while (depth > 1) {
            nodesNext = new ArrayList<>();
            for (int i = 0; i < nodesCurrent.size(); i++) {
                List<List<Integer>> pawnPositionsTemp = getPawnPositions(nodesCurrent.get(i).currentInputMatrix,
                        nodesCurrent.get(i).currentPlayer);
                generateGameTree(nodesCurrent.get(i).currentInputMatrix, nodesCurrent.get(i).currentPlayer, pawnPositionsTemp,
                        nodesCurrent.get(i).nextPostions, nodesNext);
            }
            nodesCurrent = nodesNext;
            --depth;
        }
        return nodesNext;
    }

    /*private Node runMinMax(List<Node> nodes){

    }*/

    public static void main(String[] args) {
        char[][] inputMatrix =
                {{'B','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','B','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','.'},
                {'.','.','.','.','.','.','.','.','.','.','.','.','.','.','.','W'}};

        HalmaAI halmaAI = new HalmaAI();
        List<Node> nodesFinal1 = halmaAI.generateGameTreeAtDepth(halmaAI.BLACK_PLAYER,1,inputMatrix);
        List<Node> nodesFinal2 = halmaAI.generateGameTreeAtDepth(halmaAI.BLACK_PLAYER,2,inputMatrix);
        for(int i=0;i<nodesFinal1.size();i++){
            System.out.println(nodesFinal1.get(i).currentPlayerPosition+" "+nodesFinal1.get(i).nextPostions);
        }
        for(int i=0;i<nodesFinal2.size();i++){
            System.out.println(nodesFinal2.get(i).currentPlayerPosition+" "+nodesFinal2.get(i).nextPostions);
        }

    }
}

