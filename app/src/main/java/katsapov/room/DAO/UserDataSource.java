package katsapov.room.DAO;

import java.util.List;

import io.reactivex.Flowable;
import katsapov.room.Database.IUserDataSource;
import katsapov.room.Model.User;

public class UserDataSource implements IUserDataSource {

    private UserDAO userDAO;
    private  static  UserDataSource mInstance;

    private UserDataSource(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public static UserDataSource  getInstance(UserDAO userDAO){
        if(mInstance == null){
            mInstance = new UserDataSource(userDAO);
        }
        return mInstance;
    }

    @Override
    public Flowable<User> getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    @Override
    public Flowable<List<User>> getAllUsers() {
        return userDAO.getAllUsers();
    }

    @Override
    public void insertUser(User... users) {
        userDAO.insertUser(users);
    }

    @Override
    public void updateUser(User... users) {
        userDAO.updateUser(users);
    }

    @Override
    public void deleteUser(User user) {
        userDAO.deleteUser(user);
    }

    @Override
    public void deleteAllUsers() {
        userDAO.deleteAllUsers();
    }
}
