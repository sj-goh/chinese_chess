package com.example.test.boardgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.test.chinesechess.R;
import com.example.test.chinesechess.SinglePlayer;
import com.google.firebase.database.DatabaseReference;


public class ChineseChessBoard {

    // variable to calculate padding of gameboard.
    Context context;
    Activity activity;
    int screenWidth;
    int margin;

    // for game board.
    ImageView gameBoard;
    FrameLayout gameBoardLayout;

    // board dimension
    final int boardRow = 10;
    final int boardColumn = 9;

    // used to convert index to x and y position.
    Point[][] boardPoints;

    // store piece index; -1 means no piece.
    int[][] boardPieceIndex;
    Piece[] pieces;

    // start point for first piece.
    int startX, startY;
    // dimension of piece
    int pieceSizeX, pieceSizeY;

    Drawable hostPieceImage, guestPieceImage;

    LinearLayout.LayoutParams lpPiece;
    Drawable blueBG;

    DatabaseReference gameRef;

    public ChineseChessBoard(Context context, Activity activity, Drawable hostPieceImage, Drawable guestPieceImage, DatabaseReference gameRef) {
        this.context = context;
        this.activity = activity;
        this.hostPieceImage = hostPieceImage;
        this.guestPieceImage = guestPieceImage;
        this.gameRef = gameRef;

        screenWidth = getScreenWidth();
        gameBoard = activity.findViewById(R.id.gameBoardImage);
        gameBoardLayout = activity.findViewById(R.id.gameBoardLayout);

        //5dp to pixel.
        float scale = context.getResources().getDisplayMetrics().density;
        margin = (int) (5 *scale + 0.5f);

        // board dimension:
        boardPoints = new Point[boardRow][boardColumn];
        boardPieceIndex = new int[boardRow][boardColumn];
        pieces = new Piece[32];

        // pieces are prepared when board is drawn.
        setUpBoard();
    }

    public int getScreenWidth() {
        // get screen width
        WindowManager wm = activity.getWindowManager();
        Display disp = wm.getDefaultDisplay();
        Point wp = new Point();
        disp.getSize(wp);

        return wp.x;
    }

    public void setUpBoard(){
        // calculate padding needed
        // 9 column for chinese chess
        int padding = screenWidth/boardRow/2 + margin;
        gameBoard.setPadding(padding,0,padding,0);
        // set board at centre of screen
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) gameBoard.getLayoutParams();
        params.gravity = Gravity.CENTER;
        gameBoard.setLayoutParams(params);

        // board position on screen..
        // wait till the board is drawn.
        gameBoard.post(new Runnable() {
            @Override
            public void run() {
                int[] imagePos = new int[4];
                imagePos = getBitmapPositionInsideImageView(gameBoard);

                // column x , row y
                // -1 because piece is on the line intersection.
                pieceSizeX = imagePos[2]/(boardColumn-1);
                pieceSizeY = imagePos[3]/(boardRow-1);

                startX = imagePos[0] - pieceSizeX/2;
                startY = imagePos[1] - pieceSizeY/2;

                for(int i = 0; i<boardRow; i++) {
                    for(int j = 0; j<boardColumn; j++) {

                        boardPoints[i][j] = new Point();
                        boardPoints[i][j].x = startX + j * pieceSizeX;
                        boardPoints[i][j].y = startY + i * pieceSizeY;

                        // init all points with no piece.
                        boardPieceIndex[i][j] = -1;
                    }
                }

                // set piece
                setInitialPiece();


                // todo: handler does not work for hua wei.
                // create event for board ready.
                if(activity.getLocalClassName().equals("SinglePlayer")){
                    SinglePlayer.boardReadyHandler.sendEmptyMessage(SinglePlayer.boardReady);
                }else{
                    if(gameRef!=null) {
                        gameRef.setValue(Math.random());
                    }
                }
            }
        });
    }

    void setInitialPiece(){

        lpPiece = new LinearLayout.LayoutParams(pieceSizeX,pieceSizeY);

        int backgroundId = context.getResources().getIdentifier("cell_bg", "drawable", context.getPackageName());
        blueBG = context.getResources().getDrawable(backgroundId);
        blueBG.setColorFilter( Color.BLUE, PorterDuff.Mode.MULTIPLY );

        // place piece on board.
        boardPieceIndex[0][0] = 0;
        pieces[0]= new CarPiece  (0, "guest",context, boardPoints[0][0], gameBoardLayout, lpPiece,0,0);
        boardPieceIndex[0][1] = 1;
        pieces[1]= new HorsePiece(1, "guest",context, boardPoints[0][1], gameBoardLayout, lpPiece,0,1);
        boardPieceIndex[0][2] = 2;
        pieces[2]= new XiangPiece(2,"guest",context, boardPoints[0][2], gameBoardLayout, lpPiece,0,2);
        boardPieceIndex[0][3] = 3;
        pieces[3]= new ShiPiece(3,"guest",context, boardPoints[0][3], gameBoardLayout, lpPiece,0,3);
        boardPieceIndex[0][4] = 4;
        pieces[4]= new KingPiece(4,"guest",context, boardPoints[0][4], gameBoardLayout, lpPiece, guestPieceImage,0,4);
        boardPieceIndex[0][5] = 5;
        pieces[5]= new ShiPiece  (5,"guest",context, boardPoints[0][5], gameBoardLayout, lpPiece,0,5);
        boardPieceIndex[0][6] = 6;
        pieces[6]= new XiangPiece(6,"guest",context, boardPoints[0][6], gameBoardLayout, lpPiece,0,6);
        boardPieceIndex[0][7] = 7;
        pieces[7]= new HorsePiece(7,"guest",context, boardPoints[0][7], gameBoardLayout, lpPiece,0,7);
        boardPieceIndex[0][8] = 8;
        pieces[8]= new CarPiece  (8,"guest",context, boardPoints[0][8], gameBoardLayout, lpPiece,0,8);
        boardPieceIndex[2][1] = 9;
        pieces[9] = new PaoPiece(9,"guest",context, boardPoints[2][1], gameBoardLayout, lpPiece,2,1);
        boardPieceIndex[2][7] = 10;
        pieces[10] = new PaoPiece  (10,"guest",context, boardPoints[2][7], gameBoardLayout, lpPiece,2,7);
        boardPieceIndex[3][0] = 11;
        pieces[11] = new PingPiece(11,"guest", context, boardPoints[3][0], gameBoardLayout, lpPiece,3,0);
        boardPieceIndex[3][2] = 12;
        pieces[12] = new PingPiece(12,"guest", context, boardPoints[3][2], gameBoardLayout, lpPiece,3,2);
        boardPieceIndex[3][4] = 13;
        pieces[13] = new PingPiece(13,"guest", context, boardPoints[3][4], gameBoardLayout, lpPiece,3,4);
        boardPieceIndex[3][6] = 14;
        pieces[14] = new PingPiece(14,"guest", context, boardPoints[3][6], gameBoardLayout, lpPiece,3,6);
        boardPieceIndex[3][8] = 15;
        pieces[15] = new PingPiece(15,"guest", context, boardPoints[3][8], gameBoardLayout, lpPiece,3,8);

        // host pieces
        boardPieceIndex[9][0] = 16;
        pieces[16]= new CarPiece  (16, "host",context, boardPoints[9][0], gameBoardLayout, lpPiece,9,0);
        boardPieceIndex[9][1] = 17;
        pieces[17]= new HorsePiece(17, "host",context, boardPoints[9][1], gameBoardLayout, lpPiece,9,1);
        boardPieceIndex[9][2] = 18;
        pieces[18]= new XiangPiece(18,"host",context, boardPoints[9][2], gameBoardLayout, lpPiece,9,2);
        boardPieceIndex[9][3] = 19;
        pieces[19]= new ShiPiece  (19,"host",context, boardPoints[9][3], gameBoardLayout, lpPiece,9,3);
        boardPieceIndex[9][4] = 20;
        pieces[20]= new KingPiece (20,"host",context, boardPoints[9][4], gameBoardLayout, lpPiece, hostPieceImage,9,4);
        boardPieceIndex[9][5] = 21;
        pieces[21]= new ShiPiece  (21,"host",context, boardPoints[9][5], gameBoardLayout, lpPiece,9,5);
        boardPieceIndex[9][6] = 22;
        pieces[22]= new XiangPiece(22,"host",context, boardPoints[9][6], gameBoardLayout, lpPiece,9,6);
        boardPieceIndex[9][7] = 23;
        pieces[23]= new HorsePiece(23,"host",context, boardPoints[9][7], gameBoardLayout, lpPiece,9,7);
        boardPieceIndex[9][8] = 24;
        pieces[24]= new CarPiece  (24,"host",context, boardPoints[9][8], gameBoardLayout, lpPiece,9,8);
        boardPieceIndex[7][1] = 25;
        pieces[25]= new PaoPiece  (25,"host",context, boardPoints[7][1], gameBoardLayout, lpPiece,7,1);
        boardPieceIndex[7][7] = 26;
        pieces[26]= new PaoPiece  (26,"host",context, boardPoints[7][7], gameBoardLayout, lpPiece,7,7);
        boardPieceIndex[6][0] = 27;
        pieces[27] = new PingPiece(27,"host", context, boardPoints[6][0], gameBoardLayout, lpPiece,6,0);
        boardPieceIndex[6][2] = 28;
        pieces[28] = new PingPiece(28,"host", context, boardPoints[6][2], gameBoardLayout, lpPiece,6,2);
        boardPieceIndex[6][4] = 29;
        pieces[29] = new PingPiece(29,"host", context, boardPoints[6][4], gameBoardLayout, lpPiece,6,4);
        boardPieceIndex[6][6] = 30;
        pieces[30] = new PingPiece(30,"host", context, boardPoints[6][6], gameBoardLayout, lpPiece,6,6);
        boardPieceIndex[6][8] = 31;
        pieces[31] = new PingPiece(31,"host", context, boardPoints[6][8], gameBoardLayout, lpPiece,6,8);
    }

    /////////////////////////////////////////////////////
    // getter and setter:
    // only BoardPieceIndex can be set in this class.
    public Point[][] getAllBoardPoints() {
        return boardPoints;
    }

    public Point getBoardPoint(int row, int column) {
        return boardPoints[row][column];
    }

    public int[][] getAllBoardPieceIndex() {
        return boardPieceIndex;
    }

    public int getBoardPieceIndex(int row, int column) {
        return boardPieceIndex[row][column];
    }

    public void setBoardPieceIndex(int row, int column, int index) {
        this.boardPieceIndex[row][column] = index;
    }

    public Piece[] getAllPieces() {
        return pieces;
    }

    public Piece getPieces(int index) {
        return pieces[index];
    }

    /**
     * Returns the bitmap position inside an imageView.
     * @param imageView source ImageView
     * @return 0: left, 1: top, 2: width, 3: height
     */
    public static int[] getBitmapPositionInsideImageView(ImageView imageView) {
        int[] ret = new int[4];

        if (imageView == null || imageView.getDrawable() == null)
            return ret;

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int actW = Math.round(origW * scaleX);
        final int actH = Math.round(origH * scaleY);

        ret[2] = actW;
        ret[3] = actH;

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - actH)/2;
        int left = (int) (imgViewW - actW)/2;

        ret[0] = left;
        ret[1] = top;

        return ret;
    }

}
