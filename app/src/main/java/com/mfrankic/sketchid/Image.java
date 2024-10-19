package com.mfrankic.sketchid;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "image")
public class Image {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int source;

    public Image(String name, int source) {
        this.name = name;
        this.source = source;
    }
}
