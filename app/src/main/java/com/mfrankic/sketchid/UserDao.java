package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM user WHERE userId = :userId LIMIT 1")
    User getUserById(int userId);  // Method to get a user by their ID

    @Query("SELECT * FROM user")
    List<User> getAllUsers();
}
