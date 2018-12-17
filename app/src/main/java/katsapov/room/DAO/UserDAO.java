package katsapov.room.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.reactivex.Flowable;
import katsapov.room.Model.User;

@Dao //methods work work with DB
public interface UserDAO {

    @Query("SELECT * FROM users WHERE id=:userId") //sql-reqest with parametr
    Flowable<User> getUserById(int userId);

    @Query("SELECT * FROM users")
    Flowable<List<User>> getAllUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE) //unique statement
    void insertUser(User... users);

    @Update
    void updateUser(User... users);

    @Delete
    void deleteUser(User user);

    /*@Transaction    //some metods in one transaction
    void insertUserNameAndEmail(User users, Email email){
        insertUser(users);
        insertEmail(email);
    }*/

    //liveData<List<user>>

    //converter!
   /* public class DateConverter {
        @TypeConverter
        public Long dateToTimestamp(Date date) {
            if (date == null) {
                return null;
            } else {
                return date.getTime();
            }
        }
    }*/
   /* @Dao
    public interface EmployeeDao {
        @Query("SELECT * FROM employee WHERE birthday = :birthday")
        Employee getByDate(@TypeConverters({DateConverter.class}) Date birthday);
    }*/
   /* @Dao
    public interface EmployeeDao
        @Query("SELECT * FROM employee WHERE birthday BETWEEN :birthdayFrom and :birthdayTo")
        @TypeConverters({DateConverter.class})
        Employee getByDate(Date birthdayFrom, Date birthdayTo);
    }*/

    @Query("DELETE FROM users")
    void deleteAllUsers();
}