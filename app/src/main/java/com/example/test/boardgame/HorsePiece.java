package com.example.test.boardgame;

import android.content.Context;
import android.graphics.Point;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.example.test.chinesechess.R;
import com.example.test.database.MoveData;


public class HorsePiece extends Piece {

    public HorsePiece(int pieceIndex, String pieceOwner, Context context, Point initialPos, FrameLayout gameBoardLayout, LinearLayout.LayoutParams lpPiece, int boardRow, int boardColumn) {
        super(pieceIndex, pieceOwner, context, initialPos, gameBoardLayout, lpPiece, boardRow, boardColumn);

        if(pieceOwner.equals("host")){
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ma_red));
        } else{
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ma));
        }
    }

    @Override
    public boolean isMoveValid(MoveData moveData) {
        // diff must be equal to 2.
        int deltaX;
        int deltaY;
        int delta;

        deltaX = Math.abs(moveData.endColumn-moveData.startColumn);
        deltaY = Math.abs(moveData.endRow-moveData.startRow);

        delta = deltaX+ deltaY;

        if(delta!=3){
            return false;
        }
        return true;
    }

    @Override
    public boolean isKillValid(MoveData moveData, ChineseChessBoard board) {
        int deltaColumn;
        int deltaRow;
        int nextPieceInd;

        nextPieceInd = -1;

        deltaColumn = moveData.endColumn-moveData.startColumn;
        deltaRow = moveData.endRow-moveData.startRow;

        // path cannot be blocked.
        if(Math.abs(deltaColumn)>Math.abs(deltaRow)){
            // moving horizontally
            if(deltaColumn>0){
                // move right
                nextPieceInd = board.boardPieceIndex[moveData.startRow][moveData.startColumn+1];
            } else{
                // move left
                nextPieceInd = board.boardPieceIndex[moveData.startRow][moveData.startColumn-1];
            }
        }else{
            // moving vertically.
            if(deltaRow>0){
                // move down
                nextPieceInd = board.boardPieceIndex[moveData.startRow+1][moveData.startColumn];
            }else{
                // move up
                nextPieceInd = board.boardPieceIndex[moveData.startRow-1][moveData.startColumn];
            }
        }

        if(nextPieceInd!=-1){
            return false;
        }

        return true;
    }
}
