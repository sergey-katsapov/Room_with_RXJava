package katsapov.room.DAO;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import katsapov.room.Model.User;

import static katsapov.room.DAO.UserDatabase.DATABASE_VERSION;

@Database(entities = User.class, version = DATABASE_VERSION)
public abstract class UserDatabase extends RoomDatabase {
    static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Sellwin-Room";

    public  abstract  UserDAO userDAO();
    private static UserDatabase mInstance;

    public static UserDatabase getInstance(Context context){
        if(mInstance == null){
            //Room
           mInstance = Room.databaseBuilder(context, UserDatabase.class, DATABASE_NAME)
                   .fallbackToDestructiveMigration()
                   .build();
        }
        return mInstance;
    }
}
