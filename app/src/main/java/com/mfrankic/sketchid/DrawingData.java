package com.mfrankic.sketchid;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "drawing_data",
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "userId", childColumns = "userId", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Image.class, parentColumns = "imageId", childColumns = "imageId", onDelete = ForeignKey.CASCADE)
        })
public class DrawingData {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public float x;
    public float y;
    public float pressure;

    public int userId;  // Foreign key to the User table
    public int imageId;  // Foreign key to the Image table

    public DrawingData(long timestamp, float x, float y, float pressure, int userId, int imageId) {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.userId = userId;
        this.imageId = imageId;
    }
}
