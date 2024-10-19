package com.mfrankic.sketchid;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int userId;

    public String userName;

    public User(String userName) {
        this.userName = userName;
    }

    @NonNull
    @Override
    public String toString() {
        return userName;
    }
}
