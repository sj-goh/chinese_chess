package com.example.test.boardgame;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.test.chinesechess.R;
import com.example.test.database.MoveData;


public class KingPiece extends Piece {

    public KingPiece(int pieceIndex, String pieceOwner, Context context, Point initialPos, FrameLayout gameBoardLayout, LinearLayout.LayoutParams lpPiece, Drawable kingImage, int boardRow, int boardColumn) {
        super(pieceIndex, pieceOwner, context, initialPos, gameBoardLayout, lpPiece, boardRow, boardColumn);

        // todo: replace king image with user.
        if(kingImage == null) {
            if (pieceOwner.equals("host")) {
                pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.king_red));
            } else {
                pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.king));
            }
        } else {
            pieceImage.setImageDrawable(kingImage);
        }
    }

    @Override
    public boolean isMoveValid(MoveData moveData) {

        if(!(moveData.endColumn == moveData.startColumn) && !(moveData.startRow == moveData.endRow) ){
            // can only move in a straight line.
            return false;
        }

        return true;
    }

    @Override
    public boolean isKillValid(MoveData moveData, ChineseChessBoard board) {
        // flying king condition:
        Boolean flyingKingCondition;
        flyingKingCondition = false;
        int i;
        int pieceFound = 0;

        Piece opponentKing;

         if ((Math.abs(moveData.endColumn - moveData.startColumn) > 1) ||
                (Math.abs(moveData.endRow - moveData.startRow) > 1)) {
            // move more than 1 square.
             //boardPieceIndex[9][4] = 20; host
             //boardPieceIndex[0][4] = 4; guest.
            // check for flying king condition.

             pieceFound = 0;

             if (moveData.pieceIndex == 20) {
                 // i am host
                 opponentKing = board.pieces[4];
                 if (opponentKing.boardColumn == moveData.endColumn) {
                     for (i = moveData.endRow + 1; i < moveData.startRow; i++) {
                         if (board.boardPieceIndex[i][moveData.startColumn] != -1) {
                             pieceFound++;
                         }
                     }
                 } else{
                     return false;
                 }
             } else {
                 // i am guest
                 opponentKing = board.pieces[20];
                 if (opponentKing.boardColumn == moveData.endColumn) {
                     for (i = moveData.startRow + 1; i < moveData.endRow; i++) {
                         if (board.boardPieceIndex[i][moveData.startColumn] != -1) {
                             pieceFound++;
                         }
                     }
                 }else{
                     return false;
                 }
             }

             if(pieceFound==0){
                 flyingKingCondition = true;
             }

            if(flyingKingCondition== true){
                if(moveData.endRow!=opponentKing.boardRow){
                    return false;
                }
            } else {
                return false;
            }
        } else {
            // cannot move out of the box.
            if (moveData.pieceIndex > 15) {
                // host piece
                if ((moveData.endRow < 7) || (moveData.endColumn < 3) || (moveData.endColumn > 5)) {
                    return false;
                }

            } else {
                // guest piece
                if ((moveData.endRow > 2) || (moveData.endColumn < 3) || (moveData.endColumn > 5)) {
                    return false;
                }
            }
        }
        return true;
    }
}
