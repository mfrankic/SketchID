package com.mfrankic.sketchid;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "magnetic_field_baseline_data", foreignKeys = {
    @ForeignKey(
        entity = User.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    ), @ForeignKey(
    entity = Image.class,
    parentColumns = "id",
    childColumns = "image_id",
    onDelete = ForeignKey.CASCADE
)
}, indices = {
    @Index("user_id"), @Index("image_id"), @Index("session_id"), @Index("attempt")
}
)
public class MagneticFieldBaselineData {
  @PrimaryKey(autoGenerate = true)
  public long id;

  @ColumnInfo(name = "user_id")
  public long userId;

  @ColumnInfo(name = "image_id")
  public int imageId;

  @ColumnInfo(name = "session_id")
  public String sessionId;

  @ColumnInfo(name = "attempt")
  public int attempt;

  @ColumnInfo(name = "timestamp")
  public long timestamp;

  @ColumnInfo(name = "avg_x")
  public float avgX;

  @ColumnInfo(name = "avg_y")
  public float avgY;

  @ColumnInfo(name = "avg_z")
  public float avgZ;

  @ColumnInfo(name = "duration_ms")
  public long durationMs;

  @ColumnInfo(name = "sample_count")
  public int sampleCount;

  @ColumnInfo(name = "std_dev_x")
  public float stdDevX;

  @ColumnInfo(name = "std_dev_y")
  public float stdDevY;

  @ColumnInfo(name = "std_dev_z")
  public float stdDevZ;

  public MagneticFieldBaselineData(
      long userId,
      int imageId,
      String sessionId,
      int attempt,
      long timestamp,
      float avgX,
      float avgY,
      float avgZ,
      long durationMs,
      int sampleCount,
      float stdDevX,
      float stdDevY,
      float stdDevZ
  ) {
    this.userId = userId;
    this.imageId = imageId;
    this.sessionId = sessionId;
    this.attempt = attempt;
    this.timestamp = timestamp;
    this.avgX = avgX;
    this.avgY = avgY;
    this.avgZ = avgZ;
    this.durationMs = durationMs;
    this.sampleCount = sampleCount;
    this.stdDevX = stdDevX;
    this.stdDevY = stdDevY;
    this.stdDevZ = stdDevZ;
  }
} 
