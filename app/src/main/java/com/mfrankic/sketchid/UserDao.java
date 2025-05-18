package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

  @Insert
  long insertUser(User user);

  @Query("DELETE FROM user WHERE id = :userID;")
  int deleteUser(long userID);

  @Query("SELECT * FROM user WHERE id = :userID LIMIT 1")
  User getUserByID(long userID);

  @Query("SELECT * FROM user ORDER BY id DESC")
  List<User> getAllUsers();
}
