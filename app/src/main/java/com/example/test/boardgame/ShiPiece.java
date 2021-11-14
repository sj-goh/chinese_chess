package com.example.test.boardgame;

import android.content.Context;
import android.graphics.Point;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.test.chinesechess.R;
import com.example.test.database.MoveData;


public class ShiPiece extends Piece {

    public ShiPiece(int pieceIndex, String pieceOwner, Context context, Point initialPos, FrameLayout gameBoardLayout, LinearLayout.LayoutParams lpPiece, int boardRow, int boardColumn) {
        super(pieceIndex, pieceOwner, context, initialPos, gameBoardLayout, lpPiece, boardRow, boardColumn);


        if(pieceOwner.equals("host")){
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.shi_red));
        } else{
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.shi));
        }
    }

    @Override
    public boolean isMoveValid(MoveData moveData) {
        // can only move 1 step diagonally.
        if( Math.abs(moveData.endRow-moveData.startRow) !=1){
            return false;
        }

        if( Math.abs(moveData.endColumn-moveData.startColumn) !=1){
            return false;
        }

        // cannot move outside of box
        if(moveData.pieceIndex> 15){
            // host piece
            if( (moveData.endRow<7) || (moveData.endColumn<3) || (moveData.endColumn>5)){
                return false;
            }

        } else{
            // guest piece
            if( (moveData.endRow>2) || (moveData.endColumn<3) || (moveData.endColumn>5)){
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isKillValid(MoveData moveData, ChineseChessBoard board) {
        return true;
    }
}
