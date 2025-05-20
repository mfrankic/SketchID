package com.mfrankic.sketchid;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "drawing_data")
public class DrawingData {

  @PrimaryKey(autoGenerate = true)
  public int id;
  @ColumnInfo(name = "time")
  public long time;
  @ColumnInfo(name = "x")
  public float x;
  @ColumnInfo(name = "y")
  public float y;
  @ColumnInfo(name = "action")
  public String action;
  @ColumnInfo(name = "user_id")
  public int userID;
  @ColumnInfo(name = "image_id")
  public int imageID;
  @ColumnInfo(name = "item_type")
  public Item.Type itemType;
  @ColumnInfo(name = "attempt")
  public int attempt;
  @ColumnInfo(name = "session_id")
  public String sessionID;
  @ColumnInfo(name = "size")
  public float size;
  @ColumnInfo(name = "pressure")
  public float pressure;
  @ColumnInfo(name = "orientation")
  public float orientation;

  @SuppressWarnings("unused")
  public DrawingData() {

  }

  @Ignore
  private DrawingData(Builder builder) {
    this.time = builder.time;
    this.x = builder.x;
    this.y = builder.y;
    this.action = builder.action;
    this.userID = builder.userID;
    this.imageID = builder.imageID;
    this.itemType = builder.itemType;
    this.attempt = builder.attempt;
    this.sessionID = builder.sessionID;
    this.size = builder.size;
    this.pressure = builder.pressure;
    this.orientation = builder.orientation;
  }

  public DrawingData copy() {
    return new Builder()
        .time(time)
        .x(x)
        .y(y)
        .action(action)
        .userID(userID)
        .imageID(imageID)
        .itemType(itemType)
        .attempt(attempt)
        .sessionID(sessionID)
        .size(size)
        .pressure(pressure)
        .orientation(orientation)
        .build();
  }

  public static class Builder {
    private long time;
    private float x;
    private float y;
    private String action;
    private int userID;
    private int imageID;
    private Item.Type itemType;
    private int attempt;
    private String sessionID;
    private float size;
    private float pressure;
    private float orientation;

    public Builder time(long time) {
      this.time = time;
      return this;
    }

    public Builder x(float x) {
      this.x = x;
      return this;
    }

    public Builder y(float y) {
      this.y = y;
      return this;
    }

    public Builder action(String action) {
      this.action = action;
      return this;
    }

    public Builder userID(int userID) {
      this.userID = userID;
      return this;
    }

    public Builder imageID(int imageID) {
      this.imageID = imageID;
      return this;
    }

    public Builder itemType(Item.Type itemType) {
      this.itemType = itemType;
      return this;
    }

    public Builder attempt(int attempt) {
      this.attempt = attempt;
      return this;
    }

    public Builder sessionID(String sessionID) {
      this.sessionID = sessionID;
      return this;
    }

    public Builder size(float size) {
      this.size = size;
      return this;
    }

    public Builder pressure(float pressure) {
      this.pressure = pressure;
      return this;
    }

    public Builder orientation(float orientation) {
      this.orientation = orientation;
      return this;
    }

    public DrawingData build() {
      return new DrawingData(this);
    }
  }
}
