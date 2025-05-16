package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageDao {

  @Insert
  void insertAll(List<Image> images);

  @Insert
  long insertImage(Image image);

  @Update
  void updateImage(Image image);

  @Query("SELECT * FROM image")
  List<Image> getAllImages();

  @Query("SELECT * FROM image WHERE source = :source")
  List<Image> getImagesBySource(String source);

  @Delete
  void deleteImage(Image image);

  @Query("DELETE FROM image WHERE source = 'custom' AND path NOT LIKE 'content://%'")
  void deleteInvalidCustomImages();
}
