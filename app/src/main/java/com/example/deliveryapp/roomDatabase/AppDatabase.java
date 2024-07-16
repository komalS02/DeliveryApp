package com.example.deliveryapp.roomDatabase;

import androidx.room.Database;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {DeliveryDetails.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeliveryDetailsDao deliveryDetailDao();


    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "delivery_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}

