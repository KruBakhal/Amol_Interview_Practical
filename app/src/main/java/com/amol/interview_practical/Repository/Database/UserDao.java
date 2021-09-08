package com.amol.interview_practical.Repository.Database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.amol.interview_practical.Model.User;

import java.util.List;


@Dao
public interface UserDao {

    @Query("SELECT * FROM User ORDER BY ID")
    List<User> loadAllPersons();

    @Insert
    void insertPerson(List<User> person);

    @Query("SELECT * FROM User WHERE id = :id")
    User loadPersonById(int id);

    @Query("DELETE FROM User")
    void deleteAllUsers();
}
