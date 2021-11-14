package com.example.test.boardgame;

import android.content.Context;
import android.graphics.Point;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.test.chinesechess.R;
import com.example.test.database.MoveData;

public class XiangPiece extends Piece {

    public XiangPiece(int pieceIndex, String pieceOwner, Context context, Point initialPos, FrameLayout gameBoardLayout, LinearLayout.LayoutParams lpPiece, int boardRow, int boardColumn) {
        super(pieceIndex, pieceOwner, context, initialPos, gameBoardLayout, lpPiece, boardRow, boardColumn);


        if(pieceOwner.equals("host")){
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.xiang_red));
        } else{
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.xiang));
        }
    }

    @Override
    public boolean isMoveValid(MoveData moveData) {
        // can only move 2 step diagonally.
        if( Math.abs(moveData.endRow-moveData.startRow) !=2){
            return false;
        }

        if( Math.abs(moveData.endColumn-moveData.startColumn) !=2){
            return false;
        }

        // cannot move over river
        if(pieceOwner.equals("host")){
            if(moveData.endRow<5) {
                return false;
            }
        } else {
            if(moveData.endRow>4) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isKillValid(MoveData moveData, ChineseChessBoard board) {
        // todo : if blocked.
        int deltaColumn;
        int deltaRow;
        int nextPieceInd;

        nextPieceInd = -1;

        deltaColumn = moveData.endColumn-moveData.startColumn;
        deltaRow = moveData.endRow-moveData.startRow;

        // path cannot be blocked.
        if(deltaRow<0){
            // moving upwards
            if(deltaColumn>0){
                // move right
                nextPieceInd = board.boardPieceIndex[moveData.startRow-1][moveData.startColumn+1];
            } else{
                // move left
                nextPieceInd = board.boardPieceIndex[moveData.startRow-1][moveData.startColumn-1];
            }
        }else{
            // moving downwards
            if(deltaColumn>0){
                // move right
                nextPieceInd = board.boardPieceIndex[moveData.startRow+1][moveData.startColumn+1];
            }else{
                // move left
                nextPieceInd = board.boardPieceIndex[moveData.startRow+1][moveData.startColumn-1];
            }
        }

        if(nextPieceInd!=-1){
            return false;
        }

        return true;
    }
}
