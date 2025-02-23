package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageDao {

  @Insert
  void insertAll(List<Image> images);

  @Query("SELECT * FROM image")
  List<Image> getAllImages();
}
