package katsapov.room.Database;

import java.util.List;

import io.reactivex.Flowable;
import katsapov.room.Model.User;

public interface IUserDataSource {
    Flowable<User> getUserById(int userId);
    Flowable<List<User>> getAllUsers();
    void insertUser(User... users);
    void updateUser(User... users);
    void deleteUser(User user);
    void deleteAllUsers();
}
