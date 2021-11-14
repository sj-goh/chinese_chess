package com.example.test.chinesechess;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.database.GameData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// brief : takes cares of stuff before the game starts.
// 1. create player name.
// 2. select character.
// 3. join or host game.
// 4. pass intent to multiplayer or single player activity.
public class MainActivity extends AppCompatActivity {

    // for log in
    EditText etLogin;
    Button btLogin;
    TextView tvLogin;
    String playerName;

    // room
    TextView tvRoom;
    ListView lvRoom;
    List<String> roomList;
    List<String> roomNameList;
    Button btRoom;
    EditText etRoom;
    TextView tvGameList;

    // save initial position of logIn button.
    int cancelX;

    // start
    Button btStart;

    // data base variables
    FirebaseDatabase database;
    DatabaseReference roomListRef, logInRef;
    ValueEventListener vlroomList, vllogInRef;

    // helper functions
    Helpers helpers;

    // for manipulating edit text style.
    Drawable originalDrawable;
    int colour;

    // shared preference:
    public static final String sPlayerName = "playerName";
    public static final String scharIndex = "charIndex";

    // String needed for game play:
    String charName, roomName, role;
    String guestName, hostName, guestPiece, hostPiece;
    Boolean guestEntered;

    // indicates the end for login activity.
    boolean gameStarted;

    // for test.
    Boolean test = false;

    ///////////////////////////////////////
    // main instance.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        guestEntered = false;
        gameStarted = false;

        // find view by id.
        findView();

        // helper methods
        helpers = new Helpers(this,MainActivity.this);

        // get edit text style manipulator
        manipulateET();

        // data base instance
        database = FirebaseDatabase.getInstance();

        // handles log in of player. with et and bt.
        logIn();

        // spinner to choose character:
        selectCharacter();

        // create or join game
        prepareGame();
    }

    ////////////////////////////////////////////
    // activity methods
    private void logIn() {
        // checks if the player exists and get reference.
        final String LogInAs;
        LogInAs = "Logged in as : ";

        playerName = helpers.getSharedPrefere().getString(sPlayerName,"");

        if(!playerName.equals("")){
            etLogin.setText(LogInAs + playerName);
            etLogin.setEnabled(false);
            etLogin.setGravity(Gravity.CENTER);
            btLogin.setText(R.string.btChange);
            etLogin.setBackgroundResource(android.R.color.transparent);
            tvLogin.setText("Welcome "+ playerName + "!");
        } else {
            // player is not in shared pref. Need to create one.
            etLogin.setEnabled(true);
            etLogin.setGravity(Gravity.START);
            etLogin.setBackground(originalDrawable);
            btLogin.setText("LOG IN");
        }

        // log in listener
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logging the player in when button is clicked.
                String eText = etLogin.getText().toString().replace(LogInAs, "");

                // check if the edit text is empty.
                if (eText.equals("") && etLogin.isEnabled()) {
                    Toast.makeText(MainActivity.this, "Please enter name", Toast.LENGTH_SHORT).show();
                } else {
                    helpers.closeKeyboard();
                    if (btLogin.getText().toString().equals("Change")) {
                        // user wants to change the name
                        etLogin.setEnabled(true);
                        etLogin.setText(eText);
                        etLogin.setGravity(Gravity.START);
                        etLogin.setBackground(originalDrawable);
                        btLogin.setText("LOG IN");
                    } else {
                        // get user name and disable the text edit.
                        if (playerName.equals(eText)) {
                            Toast.makeText(MainActivity.this, "Name is not changed!", Toast.LENGTH_SHORT).show();
                        } else {
                            // update shared preferences.
                            playerName = eText;

                            helpers.getSharedPreferencesEditor().putString(sPlayerName, playerName);
                            helpers.getSharedPreferencesEditor().commit();
                        }

                        // update view display
                        etLogin.setText(LogInAs + playerName);
                        etLogin.setEnabled(false);
                        etLogin.setGravity(Gravity.CENTER);
                        etLogin.setBackgroundResource(android.R.color.transparent);
                        btLogin.setText("Change");
                        tvLogin.setText("Welcome " + playerName + "!");
                        updatePlayerInfo();
                    }
                }
            }
        });
    }

    private void selectCharacter() {
        // change character
        final Spinner charDropDown = (Spinner) findViewById(R.id.start_character);
        final int savedCharInd;

        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> charDropDownAdapter = ArrayAdapter
                .createFromResource(this, R.array.char_array, R.layout.char_list_spinner);

        // Specify the layout to use when the list of choices appears
        charDropDownAdapter.setDropDownViewResource(R.layout.char_list_spinner);

        // Apply the adapter to the spinner
        charDropDown.setAdapter(charDropDownAdapter);

        // limit item to 5 only..
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(charDropDown);

            // Set popupWindow height to 500px
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }

        // get saved chosen character if available.
        savedCharInd = helpers.getSharedPrefere().getInt(scharIndex, 0);
        // select that item, so that the string charName can be initialised.
        charDropDown.setSelection(savedCharInd);

        // actions when spinner is item is selected.
        charDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(savedCharInd != position) {
                    //save
                    helpers.getSharedPreferencesEditor().putInt(scharIndex, position);
                    helpers.getSharedPreferencesEditor().commit();
                }

                charName = charDropDown.getItemAtPosition(position).toString();

                // get resource by name
                int charPicId = getResources().getIdentifier(charName, "drawable", getPackageName());

                Drawable drawable = getResources().getDrawable(charPicId);

                ImageView iv = findViewById(R.id.start_image);

                if(drawable!=null) iv.setImageDrawable(drawable);

                updatePlayerInfo();
            }

            // needs to be kept. do not delete..
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    //////////////////////////////////
    // create or join game
    private void prepareGame() {
        updateRoomList();
        hostGame();
        joinGame();
    }

    //////////////////////////////////
    // sub functions for perpare game
    private void joinGame() {
    // join room
        lvRoom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String tmp;
                int ind1, ind2;
                tmp = roomList.get(i);

                // todo : prevent game from being joined, if someone already join as guest.
                ind1 = tmp.indexOf(": ");
                ind2 = tmp.indexOf("\n");
                tmp = roomList.get(i);
                roomName = tmp.substring(ind1+2,ind2);

                String btRoomText = btRoom.getText().toString();

                if(btRoomText.equals("Create")) {
                    // create room and add yourself as player
                    role = "guest";

                    if(cancelX == -1) {
                        cancelX = (int) btRoom.getX();
                    }

                    tvRoom.setText(role + " for game in room:");
                    tvGameList.setText("waiting for host");
                    etRoom.setText(roomName);
                    etRoom.setEnabled(false);
                    etRoom.setGravity(Gravity.CENTER);
                    etRoom.setBackgroundResource(android.R.color.transparent);
                    btRoom.setText("Cancel");
                    btRoom.setX(10);

                    btStart.setVisibility(View.VISIBLE);

                    setLogInEventListener();
                }
            }
        });
    }

    private void hostGame() {
        //create room
        btRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String RoomNameTmp = etRoom.getText().toString();

                // check if room exist.
                if(!RoomNameTmp.equals("")) {
                    // clear focus so that keyboard does not pop up before click
                    helpers.closeKeyboard();

                    String btRoomText = btRoom.getText().toString();

                    if (btRoomText.equals("Create")) {
                        // create room and add yourself as player

                        if (roomNameList.contains(RoomNameTmp) && !test) {
                            // prevent room with same name from being created.
                            Toast.makeText(MainActivity.this, "Room exists. Please rename.", Toast.LENGTH_SHORT).show();
                            etRoom.setText("");
                        } else {
                            roomName = RoomNameTmp;
                            role = "host";
                            // disable list view, when hosting.
                            lvRoom.setEnabled(false);

                            tvRoom.setText(role + " for game in room:");
                            tvGameList.setText("waiting for Opponent");

                            etRoom.setEnabled(false);
                            etRoom.setGravity(Gravity.CENTER);
                            etRoom.setBackgroundResource(android.R.color.transparent);
                            // only need to do this one time.
                            if (cancelX == -1) {
                                cancelX = (int) btRoom.getX();
                            }

                            // move to left
                            btRoom.setX(10);
                            btRoom.setText("Cancel");

                            // send host data.
                            setLogInEventListener();

                            btStart.setEnabled(false);
                            btStart.setVisibility(View.VISIBLE);
                        }
                    } else {
                        cancelGame();
                    }
                }else{
                    Toast.makeText(MainActivity.this,"Please enter name", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void startSinglePlayer() {
        try {
            logInRef.removeEventListener(vllogInRef);
            roomListRef.removeEventListener(vlroomList);
        } catch(Exception e){

        }

        gameStarted = true;

        GameData gameData = new GameData("test", "host", "computer", "player", "king", charName);

        Intent intent = new Intent(this, SinglePlayer.class);
        intent.putExtra("GameData", gameData);

        startActivity(intent);
    }

    private void setLogInEventListener() {
        logInRef = database.getReference("rooms/" + roomName + "/" + "logIn");

        vllogInRef = logInRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                checkPiece(dataSnapshot, guestEntered);

                if(role.equals("host")){
                    Object guestName = helpers.getFromDB(dataSnapshot,"guestName");
                    if(guestName != null){
                        handleGuestPing(dataSnapshot);

                        // only show once
                        if(guestEntered == false) {
                            Toast.makeText(MainActivity.this, "guest has entered the room!", Toast.LENGTH_SHORT).show();
                        }

                        guestEntered = true;
                    } else{
                        if(guestEntered == true) {
                            Toast.makeText(MainActivity.this, "guest has left the room!", Toast.LENGTH_SHORT).show();
                            guestEntered = false;
                        }
                    }
                } else if(role.equals("guest")){
                    if(dataSnapshot.getValue() == null){
                        Toast.makeText(MainActivity.this,"host has left",Toast.LENGTH_SHORT).show();
                        cancelGame();
                    } else{
                        if(helpers.getFromDB(dataSnapshot,"start")!= null){
                            startGame();
                            logInRef.child("start").removeValue();
                        }
                    }

                } else{
                    // todo : do something to prevent this siuation!
                    Toast.makeText(MainActivity.this, "Login listener went wrong...",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // trigger the event.
        logInRef.child("update").setValue(Math.random());

        // give name and piece after the listener is set!
        updatePlayerInfo();
    }

    private void startGame() {
        logInRef.removeEventListener(vllogInRef);
        roomListRef.removeEventListener(vlroomList);

        gameStarted = true;

        GameData gameData = new GameData(roomName, role, guestName, hostName, guestPiece, hostPiece);

        Intent intent = new Intent(this, Multiplayer.class);
        intent.putExtra("GameData", gameData);

        startActivity(intent);
    }

    private void updatePlayerInfo(){
        // if room and role are not empty, then update database.
        try {
            if ((!roomName.equals("")) && !(role.equals(""))) {
                logInRef.child(role + "Name").setValue(playerName);
                logInRef.child(role + "Piece").setValue(charName);
            }
        } catch (Exception e){

        }
    }

    private void handleGuestPing(DataSnapshot dataSnapshot) {

        Object ping = helpers.getFromDB(dataSnapshot,"ping");

        if(ping!= null) {
            Toast.makeText(MainActivity.this, ping.toString(),Toast.LENGTH_SHORT).show();
            helpers.vibrateDevice();
            logInRef.child("ping").removeValue();
        }
    }

    private void cancelGame() {

        if(role.equals("host")) {
            // delete the room
            if(test== false) {
                database.getReference("rooms/" + roomName).removeValue();
                helpers.openKeyboard(etRoom);
            }

            guestEntered = false;
        } else if(role.equals("guest")){
            if(test== false) {
                logInRef.child("guestName").removeValue();
                logInRef.child("guestPiece").removeValue();
                logInRef.child("ping").removeValue();
            }
        }

        role = "";

        tvRoom.setText("Create or join game");
        tvGameList.setText("Game List");
        etRoom.setEnabled(true);
        etRoom.setGravity(Gravity.LEFT);
        etRoom.setBackground(originalDrawable);
        btRoom.setText("Create");
        btRoom.setX(cancelX);
        btStart.setVisibility(View.INVISIBLE);
        lvRoom.setEnabled(true);
    }

    private void updateRoomList(){
        roomListRef = database.getReference("rooms");
        roomList = new ArrayList<>();
        roomNameList = new ArrayList<>();

        // listen to room list changes.
        vlroomList = roomListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // show list of rooms.
                roomList.clear();
                roomNameList.clear();
                Iterable<DataSnapshot> rooms = dataSnapshot.getChildren();
                String roomName_ = "";
                String HostName_ = "";
                String piece_ = "";
                String GuestName_ = "";

                for(DataSnapshot room:rooms){
                    roomName_ = room.getKey();
                    // get host, guest name and piece
                    Iterable<DataSnapshot> logIn = room.getChildren();
                    for(DataSnapshot itemb:logIn) {
                        Iterable<DataSnapshot> logInTree = itemb.getChildren();

                        // this is the host tree...
                        for (DataSnapshot item : logInTree) {
                            if (item.getKey().equals("hostName")) {
                                HostName_ = item.getValue().toString();
                            }

                            if (item.getKey().equals("guestName")) {
                                GuestName_ = item.getValue().toString();
                            }

                            if (item.getKey().equals("hostPiece")) {
                                piece_ = item.getValue().toString();
                            }
                        }
                    }

                    // only show room with no guest.
                    // todo : somehow nothing is shown, if this condition is active.
                    //if(GuestName_.equals("")){
                        roomList.add("room  : " + roomName_ + "\nhost   : " + HostName_ + "\npiece : " + piece_);
                        roomNameList.add(roomName_);
                    //}
                }

                // newest room will be show first.
                Collections.reverse(roomList);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        R.layout.custom_list_view, roomList);

                lvRoom.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // make sure that players has different pieces.
    private void checkPiece(DataSnapshot dataSnapshot, boolean guestEntered) {
        Iterable<DataSnapshot> data = dataSnapshot.getChildren();

        // delete old values.
        hostPiece = "";
        guestPiece = "";
        hostName = "";
        guestName = "";

        for(DataSnapshot snapshot:data){
            if( snapshot.getKey().equals("hostPiece") ){
                hostPiece = snapshot.getValue().toString();
            }

            if( snapshot.getKey().equals("guestPiece") ){
                guestPiece = snapshot.getValue().toString();
            }

            if( snapshot.getKey().equals("guestName") ){
                guestName = snapshot.getValue().toString();
            }

            if( snapshot.getKey().equals("hostName") ){
                hostName = snapshot.getValue().toString();
            }
        }

        // check: if same, disable button and change text.
        // if not then ok.
        if(guestEntered==true || role.equals("guest")) {
            if ( hostPiece.equals(guestPiece) ) {
                btStart.setText("Same character is used! Please change!");
                btStart.setEnabled(false);
            }else if(guestPiece.equals("king_red")){
                btStart.setText("Please choose other piece for guest");
                btStart.setEnabled(false);
            }
            else {
                btStart.setEnabled(true);
                if (role.equals("host")) {
                    btStart.setText("Start Game");
                } else {
                    btStart.setText("Ping Host");
                }
            }
        } else{
            if (role.equals("host")) {
                btStart.setText("Start Game");
                btStart.setEnabled(false);

            }
        }
    }

    public void onStartClick(View view) {
        if(role.equals("host")){
            startGame();
            logInRef.child("start").setValue(Math.random());
        } else if(role.equals("guest")){
            if(btStart.getText().equals("Ping Host")){
                if(role.equals("guest")) {
                    String pingText = "start game please";

                    // guest will send ping.
                    // host will delete ping of guest.
                    logInRef.child("ping").setValue(pingText);
                }
            }
        }
    }

    // test with single player.
    // on click defined directly in layout.
    public void testAction(View view) {
        startSinglePlayer();
    }

    ///////////////////////////////////////////////////////////////////
    // helper functions:
    private void manipulateET() {
        // et style manipulation
        originalDrawable = etLogin.getBackground();
        colour = tvLogin.getCurrentTextColor();

        etLogin.setTextColor(colour);
        etRoom.setTextColor(colour);

    }

    private void findView() {
        btLogin = findViewById(R.id.logInButton);
        etLogin = findViewById(R.id.NameEdit);
        tvLogin = findViewById(R.id.LogInText);

        // room
        tvRoom = findViewById(R.id.LogInStat);
        lvRoom = findViewById(R.id.roomList);

        btRoom = findViewById(R.id.RoomButton);
        // initialise with -1 first.
        cancelX = -1;

        etRoom = findViewById(R.id.RoomEdit);
        tvGameList = findViewById(R.id.gameList);

        // start
        btStart = findViewById(R.id.LogInStartButton);
    }

    @Override
    public void onBackPressed(){
        helpers.backPressAction();
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            logInRef.removeEventListener(vllogInRef);
            roomListRef.removeEventListener(vlroomList);
        }catch (Exception e){

        }
    }

    @Override
    protected  void onResume() {
        super.onResume();

        if(vllogInRef!=null && vlroomList!=null && gameStarted==false) {
            try {
                logInRef.addValueEventListener(vllogInRef);
                roomListRef.addValueEventListener(vlroomList);
            } catch (Exception e) {

            }
        }
    }

    // delete room on exit.
    @Override
    protected  void onDestroy() {
        super.onDestroy();

        try{
            if(role.equals("host")) {
                //database.getReference().child("rooms/"+roomName).removeValue();
                logInRef.removeEventListener(vllogInRef);
                roomListRef.removeEventListener(vlroomList);
            }
        }catch (Exception e){

        }

    }
}