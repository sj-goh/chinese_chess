package com.example.test.boardgame;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.test.chinesechess.SinglePlayer;
import com.example.test.database.MoveData;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;


public class Move {

    // input
    ChineseChessBoard cboard;
    Context context;
    String role;
    DatabaseReference gameRef;

    // class variable
    String turnFor;

    List<MoveData> moveList;

    int moveIndex;

    boolean redoFlag;
    String winner;

    Boolean test = false;
    Boolean single = false;

    public Move(Context context, ChineseChessBoard cboard, String role, DatabaseReference gameRef) {
        this.context = context;
        this.cboard = cboard;
        this.role = role;
        this.gameRef = gameRef;

        // internal variable
        moveList = new ArrayList<>();
        moveIndex = -1;

        turnFor = "wait";
        redoFlag = false;

        setPieceTouchListener();

        if(gameRef==null){
            single = true;
        }
    }

    private void setPieceTouchListener() {
        for (final Piece piece: cboard.getAllPieces()){
            if(piece == null){
                continue;
            }

            piece.pieceImage.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    // event comes many times.
                    // cant move opponent piece.
                    if(!piece.pieceOwner.equals(role) && (test==false) && (single ==false) ){
                        if ((event.getAction() & MotionEvent.ACTION_MASK)== MotionEvent.ACTION_DOWN) {
                            Toast.makeText(context, "Do not move opponent piece!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }

                    if(piece.pieceOwner.equals(turnFor) || test) {
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                piece.getMoveData().setStartRow(piece.getBoardRow());
                                piece.getMoveData().setStartColumn(piece.getBoardColumn());
                                piece.getMoveData().setEventXOld((int) event.getRawX());
                                piece.getMoveData().setEventYOld((int) event.getRawY());
                                piece.getMoveData().setPieceIndex(piece.getPieceIndex());
                                piece.getMoveData().setEventDeltaX(0);
                                piece.getMoveData().setEventDeltaY(0);
                                break;

                            case MotionEvent.ACTION_MOVE:
                                // get absolute position:
                                Point startPoint = cboard.getBoardPoint(piece.getBoardRow(),piece.getBoardColumn());

                                piece.getMoveData().setEventDeltaX((int) (event.getRawX() - piece.getMoveData().getEventXOld()));
                                piece.getMoveData().setEventDeltaY((int) (event.getRawY() - piece.getMoveData().getEventYOld()));

                                // end row and column will only be updated when piece is lifted.
                                // then this value will equals to piece boardRow and column.
                                v.setX(startPoint.x + piece.getMoveData().getEventDeltaX());
                                v.setY(startPoint.y + piece.getMoveData().getEventDeltaY());
                                break;
                            case MotionEvent.ACTION_UP:
                                // place the piece at centre.
                                // calculate end index by converting delta.
                                int tmpx = 0;
                                int tmpy = 0;

                                // minor adjustment so that index is increased, if half of the piece is over the cell.
                                if (piece.getMoveData().getEventDeltaX() > 0) {
                                    tmpx = piece.getMoveData().getEventDeltaX() + cboard.pieceSizeX / 2;
                                } else {
                                    tmpx = piece.getMoveData().getEventDeltaX() - cboard.pieceSizeX / 2;
                                }

                                if (piece.getMoveData().getEventDeltaY() > 0) {
                                    tmpy = piece.getMoveData().getEventDeltaY() + cboard.pieceSizeY / 2;
                                } else {
                                    tmpy = piece.getMoveData().getEventDeltaY() - cboard.pieceSizeY / 2;
                                }

                                piece.getMoveData().setEndColumn(Math.round(tmpx / cboard.pieceSizeX) + piece.getMoveData().getStartColumn());
                                piece.getMoveData().setEndRow(Math.round(tmpy / cboard.pieceSizeY) + piece.getMoveData().getStartRow());

                                // move piece!
                                if(single){
                                    movePiece(cboard.getPieces(piece.getPieceIndex()), false);
                                } else{
                                    movePiece(cboard.getPieces(piece.getPieceIndex()), true);
                                    gameRef.child("LastMoveData").setValue(piece.moveData);
                                }
                                break;
                        }
                    } else{
                        if ((event.getAction() & MotionEvent.ACTION_MASK)== MotionEvent.ACTION_DOWN) {
                            Toast.makeText(context, "Please wait for opponent to move", Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                }
            });
        }
    }

    public void movePiece(Piece piece, Boolean sendToOpponent) {
        Boolean killValid = true;
        Boolean moveValid;

        // reset the previous kill index...
        piece.getMoveData().setKilledPieceInd(-1);

        // piece will always move inside the box.
        if(piece.getMoveData().getEndColumn()<0) {
            piece.getMoveData().setEndColumn(0);
        }

        if(piece.getMoveData().getEndRow()<0) {
            piece.getMoveData().setEndRow(0);
        }

        if(piece.getMoveData().getEndRow()> cboard.boardRow-1){
            piece.getMoveData().setEndRow(cboard.boardRow-1);
        }

        if(piece.moveData.getEndColumn()> cboard.boardColumn-1){
            piece.getMoveData().setEndColumn(cboard.boardColumn-1);
        }

        moveValid = piece.isMoveValid(piece.getMoveData());
        // check if the piece is moved.
        if( (piece.getMoveData().getEndColumn() == piece.getMoveData().getStartColumn()) &&
                (piece.getMoveData().getEndRow() == piece.getMoveData().getStartRow()) ){
            // move invalid because it is not moved.
            moveValid = false;
        }

        if(moveValid){
            // this function only check for special killing rule.
            if(piece.isKillValid(piece.getMoveData(), cboard)) {
                // check if other piece is at end position.
                piece.getMoveData().setKilledPieceInd( cboard.getBoardPieceIndex(piece.getMoveData().getEndRow(),piece.getMoveData().getEndColumn()) );

                if(piece.getMoveData().getKilledPieceInd()!=-1){
                    if(piece.getMoveData().getKilledPieceInd()>31 || piece.getMoveData().getKilledPieceInd()<0){
                        killValid = false;
                        Toast.makeText(context, "invalid kill index", Toast.LENGTH_SHORT).show();
                    } else {
                        Piece killedPiece = cboard.getPieces(piece.getMoveData().getKilledPieceInd());

                        if (killedPiece.getPieceOwner().equals(piece.getPieceOwner())) {
                            // cannot kill own piece.
                            killValid = false;
                            Toast.makeText(context, "Cannot kill own piece！", Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(context, "killed" + piece.moveData.killedPieceInd, Toast.LENGTH_SHORT).show();
                            cboard.getPieces(piece.moveData.killedPieceInd).killed();
                        }
                    }
                }
            } else {
                killValid = false;
            }
        }

        if(moveValid && killValid) {
            piece.setBoardColumn( piece.getMoveData().getEndColumn() );
            piece.setBoardRow( piece.getMoveData().getEndRow() );

            // update game board.
            cboard.setBoardPieceIndex(piece.getMoveData().getStartRow(), piece.getMoveData().getStartColumn(), -1);
            cboard.setBoardPieceIndex(piece.getMoveData().getEndRow(), piece.getMoveData().getEndColumn(),  piece.getMoveData().getPieceIndex());
            Point endPoint = cboard.getBoardPoint( piece.getMoveData().getEndRow(),piece.getMoveData().getEndColumn() );
            piece.getPieceImage().setX(endPoint.x);
            piece.getPieceImage().setY(endPoint.y);

            // moveList;
            if(redoFlag == true){
                redoFlag = false;
            }else {

                updateMoveList(piece.getMoveData());
                highLightLastMove("move");

                if(sendToOpponent == true) {
                    updateOpponentMove(piece.getMoveData());
                }
            }

            // turn rotation..
            if(sendToOpponent==true) {
                if (turnFor.equals("host")) {
                    // change turn
                    turnFor = "guest";
                } else {
                    turnFor = "host";
                }

                gameRef.child("turn").setValue(turnFor);
            }

            // for single player
            if(gameRef == null){
                SinglePlayer.boardReadyHandler.sendEmptyMessage(SinglePlayer.changeTmsg);
            }

            // check if king is killed:
            checkForWinner();

        } else{
            // return to start position:
            Point initPoint = cboard.boardPoints[piece.moveData.startRow][piece.moveData.startColumn];
            piece.pieceImage.setX(initPoint.x);
            piece.pieceImage.setY(initPoint.y);
        }
    }

    private void checkForWinner() {
        // send to database and create dialog to promt reset.
        // if not return to menu..
        if(cboard.pieces[4].isKilled == true) {
            //Toast.makeText(context,"host Wins!",Toast.LENGTH_SHORT).show();
            winner = "Host";
            pleaseResetGame();
        } else if(cboard.pieces[20].isKilled == true){
            //Toast.makeText(context,"guest Wins!",Toast.LENGTH_SHORT).show();
            winner = "Guest";
            pleaseResetGame();
        }
    }

    private void pleaseResetGame(){

        // display confirmation dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("Game Over");
        builder.setMessage(winner + " has won the game!\nUndo or reset the game!");

        builder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateOpponentMove(MoveData moveData) {
        // todo.. after valid move, update it for opponent.
        // gameRef
        if(role.equals("host")){
            gameRef.child("move2guest").setValue(moveData);
        } else {
            gameRef.child("move2host").setValue(moveData);
        }
    }

    private void updateMoveList(MoveData moveDataInput) {
        // create new instance so that the list stays as it is.
        MoveData moveData = new MoveData(moveDataInput);
        // this happens when undo is done until the board is in original state..
        // clear list to avoid unwanted behaviour.
        if(moveIndex == -1){
            moveList.clear();
        }

        moveIndex++;

        if(moveList.size()!=0 ){
            if( moveIndex == moveList.size() ) {
                moveList.add( moveData);
            } else{
                // this case happens if new move is recoreded after undo.
                moveList.set(moveIndex,moveData);
                // remove the reset of the list..
                for (int i = moveIndex+1; i<moveList.size(); i++){
                    moveList.remove(i);
                }
            }
        } else{
            // first entry
            moveList.add(moveData);
        }

        // todo: delete move list on reset.
        if(gameRef!=null) {
            gameRef.child("move").setValue(moveList);
            gameRef.child(role + "CurrentMoveIndex").setValue(moveIndex);
        }
    }

    // button to undo or redo.
    public void undoOrRedo(String action, Boolean sendToOpponent){
        MoveData currentMove;
        int currentPieceIndex;
        Piece currentPiece;

        if(action.equals("undo")){
            currentMove = moveList.get(moveIndex);
            currentPieceIndex = currentMove.pieceIndex;
            currentPiece = cboard.getPieces(currentPieceIndex);

            // reset initial position
            Point initPoint = cboard.boardPoints[currentMove.startRow][currentMove.startColumn];
            currentPiece.pieceImage.setX(initPoint.x);
            currentPiece.pieceImage.setY(initPoint.y);

            // update board data.
            cboard.boardPieceIndex[currentMove.endRow][currentMove.endColumn] = -1;
            cboard.boardPieceIndex[currentMove.startRow][currentMove.startColumn] = currentPiece.pieceIndex;

            // update piece data:
            currentPiece.boardRow = currentMove.startRow;
            currentPiece.boardColumn= currentMove.startColumn;

            // undo kill:
            if(currentMove.killedPieceInd!=-1){
                // restore killed piece.
                Piece killedPiece = cboard.pieces[currentMove.killedPieceInd];

                cboard.boardPieceIndex[killedPiece.boardRow][killedPiece.boardColumn] = currentMove.killedPieceInd;
                killedPiece.revived();
            }
            highLightLastMove("undo");
            moveIndex--;
        } else if(action.equals("redo")){
            moveIndex++;
            currentMove = moveList.get(moveIndex);
            currentPieceIndex = currentMove.pieceIndex;
            currentPiece = cboard.getPieces(currentPieceIndex);
            currentPiece.moveData = currentMove;
            redoFlag = true;
            movePiece(currentPiece, sendToOpponent);
            highLightLastMove("redo");
        }
    }

    void highLightLastMove(String action){
        MoveData currentMove, previousMove;
        int currentPieceIndex, previousPieceIndex;
        Piece currentPiece, previousPiece;

        currentMove = moveList.get(moveIndex);
        currentPieceIndex = currentMove.pieceIndex;
        currentPiece = cboard.getPieces(currentPieceIndex);

        if(action.equals("undo")){
            currentPiece.pieceImage.setBackground(null);
        } else {
            currentPiece.pieceImage.setBackground(cboard.blueBG);
        }

        if(moveIndex!=0) {
            previousMove = moveList.get(moveIndex - 1);
            previousPieceIndex = previousMove.pieceIndex;
            previousPiece = cboard.getPieces(previousPieceIndex);

            if(previousPieceIndex!= currentPieceIndex) {
                if (action.equals("undo")) {
                    previousPiece.pieceImage.setBackground(cboard.blueBG);
                } else {
                    previousPiece.pieceImage.setBackground(null);
                }
            }
        }
    }

    ////////////////////////////////
    // getter and settter
    public String getTurnFor() {
        return turnFor;
    }

    public void setTurnFor(String turnFor) {
        this.turnFor = turnFor;
    }

    public int getMoveIndex() {
        return moveIndex;
    }

    public int getMoveListSize(){
        return moveList.size();
    }


    public List<MoveData> getMoveList() {
        return moveList;
    }
}
