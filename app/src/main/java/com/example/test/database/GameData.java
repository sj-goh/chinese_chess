package com.example.test.database;

import android.os.Parcel;
import android.os.Parcelable;

// calss to save data to database.
public class GameData implements Parcelable {

    // String needed for game play:
    String roomName, role;

    String guestName, hostName, guestPiece, hostPiece;

    public GameData(String roomName, String role, String guestName, String hostName, String guestPiece, String hostPiece) {
        this.roomName = roomName;
        this.role = role;
        this.guestName = guestName;
        this.hostName = hostName;
        this.guestPiece = guestPiece;
        this.hostPiece = hostPiece;
    }

    protected GameData(Parcel in) {
        roomName = in.readString();
        role = in.readString();
        guestName = in.readString();
        hostName = in.readString();
        guestPiece = in.readString();
        hostPiece = in.readString();
    }

    public static final Creator<GameData> CREATOR = new Creator<GameData>() {
        @Override
        public GameData createFromParcel(Parcel in) {
            return new GameData(in);
        }

        @Override
        public GameData[] newArray(int size) {
            return new GameData[size];
        }
    };

    public String getRoomName() {
        return roomName;
    }

    public String getRole() {
        return role;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getHostName() {
        return hostName;
    }

    public String getGuestPiece() {
        return guestPiece;
    }

    public String getHostPiece() {
        return hostPiece;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(roomName);
        parcel.writeString(role);
        parcel.writeString(guestName);
        parcel.writeString(hostName);
        parcel.writeString(guestPiece);
        parcel.writeString(hostPiece);
    }
}
