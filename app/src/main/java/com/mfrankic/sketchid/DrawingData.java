package com.mfrankic.sketchid;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "drawing_data")
public class DrawingData {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public long time;
    public float x;
    public float y;
    public String action;
    @ColumnInfo(name = "user_id")
    public int userID;
    @ColumnInfo(name = "image_id")
    public int imageID;
    @ColumnInfo(name = "item_type")
    public Item.Type itemType;
    public int attempt;

    public DrawingData(
            long time,
            float x,
            float y,
            String action,
            int userID,
            int imageID,
            Item.Type itemType,
            int attempt
    ) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.action = action;
        this.userID = userID;
        this.imageID = imageID;
        this.itemType = itemType;
        this.attempt = attempt;
    }

    public DrawingData copy() {
        return new DrawingData(time, x, y, action, userID, imageID, itemType, attempt);
    }
}
