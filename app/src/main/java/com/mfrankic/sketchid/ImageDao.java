package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ImageDao {

    @Insert
    void insertImage(Image image);

    @Query("SELECT * FROM image WHERE imageId = :imageId LIMIT 1")
    Image getImageById(int imageId);  // Method to get an image by its ID

    @Query("SELECT * FROM image")
    List<Image> getAllImages();
}
