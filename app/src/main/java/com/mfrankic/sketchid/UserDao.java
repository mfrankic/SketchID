package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Data Access Object (DAO) for the {@link User} entity.
 * Provides methods to interact with the user table in the database.
 */
@Dao
public interface UserDao {

  /**
   * Inserts a new user into the database.
   *
   * @param user The user to insert.
   * @return The row ID of the newly inserted user.
   */
  @Insert
  long insertUser(User user);

  /**
   * Deletes a user from the database based on their ID.
   *
   * @param userID The ID of the user to delete.
   * @return The number of rows affected (should be 1 if successful, 0 otherwise).
   */
  @Query("DELETE FROM user WHERE id = :userID;")
  int deleteUser(long userID);

  /**
   * Retrieves a user from the database by their ID.
   *
   * @param userID The ID of the user to retrieve.
   * @return The {@link User} object if found, or null otherwise.
   */
  @Query("SELECT * FROM user WHERE id = :userID LIMIT 1")
  User getUserByID(long userID);

  /**
   * Retrieves all users from the database, ordered by ID in descending order.
   *
   * @return A list of all {@link User} objects.
   */
  @Query("SELECT * FROM user ORDER BY id DESC")
  List<User> getAllUsers();
}
