package com.mfrankic.sketchid;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "image")
public class Image {

    @PrimaryKey(autoGenerate = true)
    public int imageId;

    public String imageName;
    public String imageSource;  // E.g., "@drawable/sun"

    public Image(String imageName, String imageSource) {
        this.imageName = imageName;
        this.imageSource = imageSource;
    }
}
