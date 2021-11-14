package com.example.test.boardgame;

import android.content.Context;
import android.graphics.Point;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.test.chinesechess.R;
import com.example.test.database.MoveData;

public class PaoPiece extends Piece {

    public PaoPiece(int pieceIndex, String pieceOwner, Context context, Point initialPos, FrameLayout gameBoardLayout, LinearLayout.LayoutParams lpPiece, int boardRow, int boardColumn) {
        super(pieceIndex, pieceOwner, context, initialPos, gameBoardLayout, lpPiece, boardRow, boardColumn);


        if(pieceOwner.equals("host")){
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.pao_red));
        } else{
            pieceImage.setImageDrawable(context.getResources().getDrawable(R.drawable.pao));
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

        int killPieceInd = board.boardPieceIndex[moveData.endRow][moveData.endColumn];
        int i;
        int pieceFound;

        if(killPieceInd!=-1){
            pieceFound= 0;
            // cannot kill if no piece in centre.
            // cannot move over piece.
            if(moveData.endColumn!=moveData.startColumn){
                // moving horizontally.
                if(moveData.startColumn<moveData.endColumn) {
                    for (i = moveData.startColumn+1; i < moveData.endColumn; i++) {
                        if (board.boardPieceIndex[moveData.startRow][i] != -1) {
                            pieceFound++;
                        }
                    }
                } else{
                    for (i = moveData.endColumn+1; i < moveData.startColumn; i++) {
                        if (board.boardPieceIndex[moveData.startRow][i] != -1) {
                            pieceFound++;
                        }
                    }
                }
            }
            else if(moveData.endRow!=moveData.startRow){
                // move vertically.
                if(moveData.startRow<moveData.endRow) {
                    for (i = moveData.startRow+1; i < moveData.endRow; i++) {
                        if (board.boardPieceIndex[i][moveData.startColumn] != -1) {
                            pieceFound++;
                        }
                    }
                } else
                {
                    for (i = moveData.endRow+1; i < moveData.startRow; i++) {
                        if (board.boardPieceIndex[i][moveData.startColumn] != -1) {
                            pieceFound++;
                        }
                    }
                }
            }

            if(pieceFound !=1) {
                return false;
            }
        }

        return true;
    }
}
