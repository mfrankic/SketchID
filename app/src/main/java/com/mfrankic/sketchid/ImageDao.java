package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) for {@link Image} entities.
 * Provides methods to interact with the image table in the database.
 */
@Dao
public interface ImageDao {

  /**
   * Inserts a list of images into the database.
   *
   * @param images A list of {@link Image} objects to insert.
   */
  @Insert
  void insertAll(List<Image> images);

  /**
   * Inserts a single image into the database.
   *
   * @param image The {@link Image} object to insert.
   * @return The row ID of the newly inserted image.
   */
  @Insert
  long insertImage(Image image);

  /**
   * Updates an existing image in the database.
   *
   * @param image The {@link Image} object to update.
   */
  @Update
  void updateImage(Image image);

  /**
   * Retrieves all images from the database.
   *
   * @return A list of all {@link Image} objects.
   */
  @Query("SELECT * FROM image")
  List<Image> getAllImages();

  /**
   * Retrieves all images from a specific source.
   *
   * @param source The source string to filter images by (e.g., "custom", "url").
   * @return A list of {@link Image} objects from the specified source.
   */
  @Query("SELECT * FROM image WHERE source = :source")
  List<Image> getImagesBySource(String source);

  /**
   * Deletes an image from the database.
   *
   * @param image The {@link Image} object to delete.
   */
  @Delete
  void deleteImage(Image image);

  /**
   * Deletes custom images from the database that have invalid paths.
   * An invalid path is one that is not a content URI or a file URI.
   * This is used to clean up potentially broken image references.
   */
  @Query(
      "DELETE FROM image\n"
      + "WHERE source = '"
      + Constants.SOURCE_CUSTOM
      + "'\n"
      + "AND path NOT LIKE 'content://%'\n"
      + "AND path NOT LIKE 'file://%'"
  )
  void deleteInvalidCustomImages();
}
