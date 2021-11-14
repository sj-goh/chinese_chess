package com.example.test.chinesechess;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.boardgame.ChineseChessBoard;
import com.example.test.boardgame.Move;
import com.example.test.database.MoveData;
import com.example.test.database.GameData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// brief :
// handles activity for multiplayer gameplay: database and ui.
public class Multiplayer extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference gameRef;
    DatabaseReference readyRef, moveRef, turnRef, uiRef, uiAnsRef;
    ValueEventListener vlreadyRef, vlmoveRef, vlturnRef, vluiRef, vluiAnsRef;

    // Views
    TextView tvRole, tvTopName, tvBotName;
    ImageView ivTopPiece, ivBotPiece;
    TextView tvTurn;

    TextView waitTextView;

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

    // game play variables
    Boolean gameStarted;

    // replace king piece by this image.
    Drawable hostPieceImage, guestPieceImage;

    // helper to flip host or guest. eg: role = host, opponent = guest.
    String opponent;

    Helpers helpers;

    ChineseChessBoard cboard;
    Move move;

    // todo: how to detect. how to hanldle?
    // opponent left
    boolean opponentLeft = false;

    boolean uiSender;
    Drawable greenBG;


    boolean test = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer);

        // only for host.
        gameStarted = false;
        opponentLeft = false;

        // helper methods
        helpers = new Helpers(this,Multiplayer.this);

        // get data from previous activity
        findView();
        updateGameData();

        // database
        database = FirebaseDatabase.getInstance();
        gameRef =  database.getReference("rooms/" + roomName+ "/game");

        prepareGameBoard();

        boardReadyListener();

        uiSender = false;

        greenBG = getResources().getDrawable(R.drawable.cell_bg);
        greenBG.setColorFilter( Color.GREEN, PorterDuff.Mode.MULTIPLY );

    }

    // use data base to check if board is ready.
    private void boardReadyListener() {
        gameRef.child(role+"BoardReady").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null) {
                    gameRef.child(role + "Stat").setValue("ready");
                    // set move data here!
                    move = new Move(Multiplayer.this, cboard, role, gameRef);

                    // prepare other listener.
                    readyEventListener();
                    uiEventListener();
                    movePieceEventListener();
                    turnEventListener();
                    uiAnsEventListener();
                    enableUIButton();
                    setWaitTextView();
                    gameRef.child(role + "BoardReady").removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /////////////////////////////
    // main method
    private void prepareGameBoard() {
        // draw board and piece.
        // todo : flip colour for host and guest randomly.
        cboard = new ChineseChessBoard(Multiplayer.this, this,hostPieceImage, guestPieceImage, gameRef.child(role+"BoardReady"));
    }

    private void readyEventListener() {
        readyRef = database.getReference("rooms/" + roomName+ "/game/" + opponent + "Stat");

        vlreadyRef = readyRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    // only check once...
                    if(gameStarted == false) {
                        if (role.equals("host")) {
                            if (dataSnapshot.getValue().toString().contains("ready")) {
                                gameStarted = true;
                                if(role.equals("host")) {
                                    if (Math.random() > 0.5) {
                                        move.setTurnFor("host");
                                    } else {
                                        move.setTurnFor("guest");
                                    }

                                    turnRef.setValue(move.getTurnFor());
                                }
                            }
                        }
                    } else {// end of game started == false

                        // todo : add this part for restore game option.
                        // problem : room is not deleted because update is sent after delete.
                        // valid for both host and guest.
//                        if (dataSnapshot.getValue().toString().contains("left")) {
//                            Toast.makeText(Multiplayer.this, opponent + " has left the game!", Toast.LENGTH_SHORT).show();
//                            database.getReference().child("rooms/" + roomName).removeValue();
//                            // bring user back to login screen.
//                            opponentLeft = true;
//                            Intent intent = new Intent(Multiplayer.this, MainActivity.class);
//                            startActivity(intent);
//                            //finish();
//                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void turnEventListener(){
        turnRef =  database.getReference("rooms/" + roomName+ "/game/turn" );

        vlturnRef = turnRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue()!=null ) {
                    tvTurn.setText("Turn of " + dataSnapshot.getValue().toString());
                    try {
                        move.setTurnFor(dataSnapshot.getValue().toString());
                        if( move.getTurnFor().contains("host") ){
                            ivTopPiece.setBackground(null);
                            ivBotPiece.setBackground(greenBG);
                        }else {
                            ivTopPiece.setBackground(greenBG);
                            ivBotPiece.setBackground(null);
                        }

                    } catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    };

    private void movePieceEventListener() {
        moveRef =  database.getReference("rooms/" + roomName+ "/game/" + "move2" + role);
        // remove old value.
        moveRef.removeValue();

        vlmoveRef = moveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    MoveData moveData = dataSnapshot.getValue(MoveData.class);

                    cboard.getPieces(moveData.getPieceIndex()).setMoveData(moveData);
                    move.movePiece(cboard.getPieces(moveData.getPieceIndex()), false);

                    // this is sent from opponent.
                    moveRef.removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // this is to handle request.
    private void uiEventListener() {
        uiRef = database.getReference("rooms/" + roomName + "/game/ui");

        vluiRef = uiRef.child(opponent).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null){
                    String cmd = dataSnapshot.getValue().toString();

                    // this is sent from opponent.
                    try {
                        if(cmd.contains("vibrate")){
                            // allow to vibrate directly.
                            helpers.vibrateDevice();
                            // refresh stat.
                            gameRef.child(role + "Stat").setValue("ready"+ Math.random());
                        } else{
                            helpers.confirmAlertDialog( cmd.toString(), uiAnsRef.child("guest"), uiAnsRef.child("host"));
                        }
                    } catch (Exception e){

                    }

                    if(!test) {
                        uiRef.child(opponent).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // this is to respond to request
    private void uiAnsEventListener() {
        uiAnsRef = database.getReference("rooms/" + roomName + "/game/uiAns");

        vluiAnsRef = uiAnsRef.child(opponent).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue()!=null){
                    String cmd = dataSnapshot.getValue().toString();

                    // this is sent from opponent.
                    try {
                        hideWaitTextView();

                        if(cmd.toLowerCase().contains("deny")){
                            if(uiSender==true) {
                                helpers.denyAlertDialog(cmd.replace("deny", ""));
                            }
                        }else{
                            if (cmd.contains("undo")) {
                                changeTurn();
                                move.undoOrRedo("undo",false);
                            } else if (cmd.contains("redo")) {
                                move.undoOrRedo("redo", false);
                            } else if (cmd.contains("reset")) {
                                Intent intent = getIntent();
                                finish();
                                gameRef.child( role + "Stat").removeValue();
                                moveRef.removeValue();
                                startActivity(intent);
                            } else if(cmd.contains("vibrate")){
                                // not possible.. something is wrong if this is received.
                            }
                        }
                    } catch (Exception e){

                    }

                    if(!test) {
                        uiAnsRef.child(opponent).removeValue();
                    }

                    uiSender = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //////////////////
    // helpers
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

    ////////////////////////////////////////////////
    // UI controls
    // when button is pressed.
    public void redoAction(View view) {
        int current_ind;

        current_ind = move.getMoveIndex() + 1;

        if ( current_ind== move.getMoveListSize() ) {
            Toast.makeText(Multiplayer.this, "Unable to redo!", Toast.LENGTH_SHORT).show();
        } else{
            uiRef.child(role).setValue("redo");
            showWaitTextView("redo");
        }
    }

    public void undoAction(View view) {
        int current_ind;

        current_ind = move.getMoveIndex() - 1;

        if (current_ind<-1){
            Toast.makeText(Multiplayer.this, "Unable to undo!", Toast.LENGTH_SHORT).show();
        } else{

            // send to opponent:
            uiRef.child(role).setValue("undo");
            showWaitTextView("undo");
        }
    }

    public void vibrateAction(View view){
        uiRef.child(role).setValue("vibrate");
    }

    public void resetAction(View view) {
        // reset game.
        uiRef.child(role).setValue("reset");
        showWaitTextView("reset");
    }

    void showWaitTextView(String action){
        // todo : deactivate game board.
        waitTextView.setText("Please wait for opponent to confirm " + action + ".");
        waitTextView.setVisibility(View.VISIBLE);
        disableUIButton();
        uiSender = true;
    }

    void hideWaitTextView(){
        waitTextView.setVisibility(View.GONE);
        enableUIButton();
    }

    void setWaitTextView(){
        // place text view in the middle of the phone.
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(50,0,50,0);

        Drawable tvBG = getResources().getDrawable(R.drawable.cell_bg);
        tvBG.setColorFilter( Color.LTGRAY, PorterDuff.Mode.MULTIPLY );

        waitTextView = new TextView(this);
        waitTextView.setTextSize(20);
        waitTextView.setTypeface(null, Typeface.BOLD);
        // for testing
        waitTextView.setLayoutParams(params);
        waitTextView.setBackground(tvBG);
        waitTextView.setGravity(Gravity.CENTER);

        gameBoardLayout.addView(waitTextView);
        hideWaitTextView();
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

    void disableUIButton(){
        Button undo = findViewById(R.id.gameUndo);
        Button redo = findViewById(R.id.gameRedo);
        Button reset = findViewById(R.id.gameRestart);
        Button vibrate = findViewById(R.id.vibrate);

        undo.setEnabled(false);
        redo.setEnabled(false);
        reset.setEnabled(false);
        vibrate.setEnabled(true);
    }

    public void changeTurn(){
        if(move.getTurnFor().equals("host")){
            move.setTurnFor("guest");
        } else {
            move.setTurnFor("host");
        }

        turnRef.setValue(move.getTurnFor());
    }

    public void onBackPressed(){
        helpers.backPressAction();
    }

    // clean the garbage!
    @Override
    protected void onStop() {
        super.onStop();

        try{
            removeListeners();
        }catch (Exception e){

        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        try {
            addListeners();
        }catch (Exception e){

        }
    }

    // todo: edit this if restore game option is avaliable.
    // delete room on exit.
    @Override
    protected  void onDestroy() {
        super.onDestroy();

        try{
            // backup to data bank.
            if(role.equals("host")) {
                //database.getReference().child("rooms/"+roomName).removeValue();
                DatabaseReference refTemp;
                refTemp = database.getReference().child("moveStore").push();
                refTemp.child("moveList").setValue(move.getMoveList());
                refTemp.child("players/hostName").setValue(hostName);
                refTemp.child("players/guestName").setValue(guestName);
                refTemp.child("players/hostPiece").setValue(hostPiece);
                refTemp.child("players/guestPiece").setValue(guestPiece);
            }

            if(opponentLeft == false) {
                gameRef.child(role + "Stat").setValue("left" + Math.random());
            }

            removeListeners();

        }catch (Exception e){

        }
    }

    void removeListeners(){
        // remove listeners.
        moveRef.removeEventListener(vlmoveRef);
        turnRef.removeEventListener(vlturnRef);
        uiRef.removeEventListener(vluiRef);
        uiAnsRef.removeEventListener(vluiAnsRef);
        readyRef.removeEventListener(vlreadyRef);
    }

    void addListeners(){
        //gameRef.addValueEventListener(vlgameRef);
        moveRef.addValueEventListener(vlmoveRef);
        turnRef.addValueEventListener(vlturnRef);
        uiRef.addValueEventListener(vluiRef);
        uiAnsRef.addValueEventListener(vluiAnsRef);
        readyRef.addValueEventListener(vlreadyRef);
    }
}
