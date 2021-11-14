package com.example.test.database;


public class MoveData {

    // index
    public int startRow;
    public int endRow;
    public int startColumn;
    public int endColumn;

    int eventXOld;
    int eventYOld;

    int eventDeltaX;
    int eventDeltaY;

    public int pieceIndex;
    public int killedPieceInd;

    public MoveData() {
        killedPieceInd =-1;
    }

    // copy constructor
    public MoveData(MoveData moveData) {
        this.startRow = moveData.startRow;
        this.endRow = moveData.endRow;
        this.startColumn = moveData.startColumn;
        this.endColumn = moveData.endColumn;
        this.eventXOld = moveData.eventXOld;
        this.eventYOld = moveData.eventYOld;
        this.eventDeltaX = moveData.eventDeltaX;
        this.eventDeltaY = moveData.eventDeltaY;
        this.pieceIndex = moveData.pieceIndex;
        this.killedPieceInd = moveData.killedPieceInd;
    }


    // getter and setter
    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }

    public int getEventXOld() {
        return eventXOld;
    }

    public void setEventXOld(int eventXOld) {
        this.eventXOld = eventXOld;
    }

    public int getEventYOld() {
        return eventYOld;
    }

    public void setEventYOld(int eventYOld) {
        this.eventYOld = eventYOld;
    }

    public int getEventDeltaX() {
        return eventDeltaX;
    }

    public void setEventDeltaX(int eventDeltaX) {
        this.eventDeltaX = eventDeltaX;
    }

    public int getEventDeltaY() {
        return eventDeltaY;
    }

    public void setEventDeltaY(int eventDeltaY) {
        this.eventDeltaY = eventDeltaY;
    }

    public int getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(int pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    public int getKilledPieceInd() {
        return killedPieceInd;
    }

    public void setKilledPieceInd(int killedPieceInd) {
        this.killedPieceInd = killedPieceInd;
    }
}