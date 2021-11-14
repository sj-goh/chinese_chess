package com.example.test.boardgame;

import android.content.Context;
import android.graphics.Point;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.test.chinesechess.R;
import com.example.test.database.MoveData;


public class PingPiece  extends Piece {

    public PingPiece(int pieceIndex, String pieceOwner, Context context, Point initialPos, FrameLayout gameBoardLayout, LinearLayout.LayoutParams lpPiece, int boardRow, int boardColumn) {
        super(pieceIndex, pieceOwner, context, initialPos, gameBoardLayout, lpPiece, boardRow, boardColumn);


        if(pieceOwner.equals("host")){
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ping_red));
        } else{
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.ping));
        }
    }

    @Override
    public boolean isMoveValid(MoveData moveData) {

        if( (Math.abs(moveData.endColumn-moveData.startColumn)>1)||
                (Math.abs(moveData.endRow-moveData.startRow)>1) ){
            // cannot move more than 1 square
            return false;
        }


        // cannot move horizontally or backwards.
        if(pieceOwner.equals("host")){
            // cannot move backwards
            if(moveData.endRow>moveData.startRow){
                return false;
            }

            // cannot move horizontally
            if(moveData.endRow>4){
                if(moveData.endColumn!=moveData.startColumn){
                    return false;
                }
            }
        } else if(pieceOwner.equals("guest")){
            // cannot move backwards
            if(moveData.endRow<moveData.startRow){
                return false;
            }

            // cannot move horizontally
            if(moveData.endRow<=4){
                if(moveData.endColumn!=moveData.startColumn){
                    return false;
                }
            }

        }

        return true;
    }

    @Override
    public boolean isKillValid(MoveData moveData, ChineseChessBoard board) {
        return true;
    }

}
