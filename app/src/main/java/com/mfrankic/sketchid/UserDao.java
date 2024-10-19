package com.mfrankic.sketchid;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Query("SELECT * FROM user ORDER BY id DESC")
    List<User> getAllUsers();
}
