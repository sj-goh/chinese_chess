package com.example.test.chinesechess;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.boardgame.ChineseChessBoard;
import com.example.test.boardgame.Move;
import com.example.test.database.GameData;

// brief:
// as first step to check if the board is working.
// for testing purpose.
// split from multiplayer, as single player does not use database.
public class SinglePlayer extends AppCompatActivity {

    //////////////////////////////////////
    // global variables
    /////////////////////////////////////

    // Views
    TextView tvRole, tvTopName, tvBotName;
    ImageView ivTopPiece, ivBotPiece;
    TextView tvTurn;

    // player data
    String roomName = "";
    String role = "";

    String hostName = "";
    String guestName = "";
    String hostPiece = "";
    String guestPiece = "";

    // gameboard
    ImageView boardImageView;
    FrameLayout gameBoardLayout;

    // replace king piece by this image.
    Drawable hostPieceImage, guestPieceImage;

    // helper to flip host or guest. eg: role = host, opponent = guest.
    String opponent;

    Helpers helpers;

    ChineseChessBoard cboard;
    Move move;

    // thread to detect board ready.
    HandlerThread handlerThread;
    public static Handler boardReadyHandler;
    public static int boardReady = 1;
    public static int changeTmsg;

    Drawable greenBG;

    String startTurn;

    //////////////////////////////
    // activity instance.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signleplayer);

        // helper methods
        helpers = new Helpers(this,SinglePlayer.this);

        greenBG = getResources().getDrawable(R.drawable.cell_bg);
        greenBG.setColorFilter( Color.GREEN, PorterDuff.Mode.MULTIPLY );

        // get data from previous activity
        findView();
        updateGameData();
        prepareGameBoard();

        setStartTurn();

        // send to data base to notify that board is ready.
        handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();
        boardReadyHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == boardReady){
                    // set move data here!
                    move = new Move( SinglePlayer.this, cboard, role, null);
                    move.setTurnFor(startTurn);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Stuff that updates the UI
                            enableUIButton();
                        }
                    });

                } else if(msg.what == changeTmsg ){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Stuff that updates the UI
                            changeTurn();
                        }
                    });
                }
            }
        };
    }

    /////////////////////////////
    // main method
    private void setStartTurn() {
        if (Math.random() > 0.5) {
            startTurn = "host";
            ivBotPiece.setBackground(greenBG);
        } else {
            startTurn = "guest";
            ivTopPiece.setBackground(greenBG);
        }

        tvTurn.setText("Turn of " + startTurn);
    }

    private void prepareGameBoard() {
        // draw board and piece.
        // todo : flip colour for host and guest randomly.
        cboard = new ChineseChessBoard(SinglePlayer.this, this,hostPieceImage, guestPieceImage, null);
    }

    public void undoAction(View view) {
        int current_ind;

        current_ind = move.getMoveIndex() - 1;

        if (current_ind<-1){
            Toast.makeText(SinglePlayer.this, "Unable to undo!", Toast.LENGTH_SHORT).show();
        } else{
            changeTurn();
            move.undoOrRedo("undo",false);
        }
    }

    // when button is pressed.
    public void redoAction(View view) {
        int current_ind;

        current_ind = move.getMoveIndex() + 1;

        if ( current_ind== move.getMoveListSize() ) {
            Toast.makeText(SinglePlayer.this, "Unable to redo!", Toast.LENGTH_SHORT).show();
        } else{
            move.undoOrRedo("redo",false);
        }
    }

    public void vibrateAction(View view){
        helpers.vibrateDevice();
    }

    public void resetAction(View view) {
        // reset game.
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    //////////////////
    // helpers
    public void changeTurn(){

        if(move.getTurnFor().equals("host")){
            move.setTurnFor("guest");
            ivTopPiece.setBackground(greenBG);
            ivBotPiece.setBackground(null);

        } else {
            move.setTurnFor("host");
            ivTopPiece.setBackground(null);
            ivBotPiece.setBackground(greenBG);
        }

        tvTurn.setText("Turn of " + move.getTurnFor());
    }

    void enableUIButton(){
        Button undo = findViewById(R.id.gameUndo);
        Button redo = findViewById(R.id.gameRedo);
        Button reset = findViewById(R.id.gameRestart);
        Button vibrate = findViewById(R.id.vibrate);

        undo.setEnabled(true);
        redo.setEnabled(true);
        reset.setEnabled(true);
        vibrate.setEnabled(true);
    }

    private void updateGameData() {
        // important: GameData class must implement parcelable.
        // game data from login
        GameData gameData;
        gameData = getIntent().getParcelableExtra("GameData");

        roomName = gameData.getRoomName();
        hostName = gameData.getHostName();
        hostPiece = gameData.getHostPiece();
        guestName = gameData.getGuestName();
        guestPiece = gameData.getGuestPiece();
        role = gameData.getRole();

        if(role.equals("host")) {
            opponent = "guest";
        } else {
            opponent = "host";
        }

        tvRole.setText("I am " + role + "!");

        // todo : player is always at the bottom...
        tvBotName.setText(hostName);
        hostPieceImage = helpers.getDrawableFromName(hostPiece);
        ivBotPiece.setImageDrawable(hostPieceImage);

        tvTopName.setText(guestName);
        guestPieceImage = helpers.getDrawableFromName(guestPiece);
        ivTopPiece.setImageDrawable(guestPieceImage);
    }

    private void findView() {
        tvRole = findViewById(R.id.roleDisplay);

        tvTopName = findViewById(R.id.topName);
        ivTopPiece = findViewById(R.id.topPic);

        tvBotName = findViewById(R.id.botName);
        ivBotPiece = findViewById(R.id.botPic);

        tvTurn = findViewById(R.id.turnDisplay);
        gameBoardLayout = findViewById(R.id.gameBoardLayout);

        boardImageView = findViewById(R.id.gameBoardImage);
    }
}
