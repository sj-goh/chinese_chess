package com.example.test.boardgame;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.test.database.MoveData;

// individual piece. Refer with index.
// get absolute position from Board class
public abstract class Piece {
    ImageView pieceImage;
    int pieceIndex;

    // guest or host
    String pieceOwner;

    int boardRow, boardColumn;

    MoveData moveData;

    boolean isKilled;

    public Piece(int pieceIndex, String pieceOwner, Context context, Point initialPos, FrameLayout gameBoardLayout, LinearLayout.LayoutParams lpPiece, int boardRow, int boardColumn) {
        this.pieceOwner = pieceOwner;
        this.pieceIndex = pieceIndex;

        this.boardRow = boardRow;
        this.boardColumn = boardColumn;

        this.isKilled = false;

        // create new instance for the image view.
        this.pieceImage = new ImageView(context);

        this.moveData = new MoveData();

        // set initial position
        this.pieceImage.setX(initialPos.x);
        this.pieceImage.setY(initialPos.y);

        gameBoardLayout.addView(this.pieceImage, lpPiece);
    }


    public void killed(){
        pieceImage.setVisibility(View.INVISIBLE);
        isKilled = true;
    }

    public void revived(){
        pieceImage.setVisibility(View.VISIBLE);
        isKilled = false;
    }

    ///////////////////////////////////
    // abstract method
    // check if piece is moving in correct pattern.
    public abstract boolean isMoveValid(MoveData moveData);

    // move that needs to consider other pieces.
    public abstract boolean isKillValid(MoveData moveData, ChineseChessBoard board);


    //////////////////////////////////
    // getter and setter
    public ImageView getPieceImage() {
        return pieceImage;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    public String getPieceOwner() {
        return pieceOwner;
    }

    public void setPieceOwner(String pieceOwner) {
        this.pieceOwner = pieceOwner;
    }

    public int getBoardRow() {
        return boardRow;
    }

    public void setBoardRow(int boardRow) {
        this.boardRow = boardRow;
    }

    public int getBoardColumn() {
        return boardColumn;
    }

    public void setBoardColumn(int boardColumn) {
        this.boardColumn = boardColumn;
    }

    public MoveData getMoveData() {
        return moveData;
    }

    public void setMoveData(MoveData moveData) {
        this.moveData = moveData;
    }

    public boolean isKilled() {
        return isKilled;
    }
}