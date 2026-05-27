package com.example.medicitaapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        FormulaRequestEntity::class,
        NotificationEntity::class,
        MedicineEntity::class
    ],
    version = 8,  // ✅ Cambia el número de versión (ej: de 7 a 8)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun formulaRequestDao(): FormulaRequestDao
    abstract fun notificationDao(): NotificationDao
    abstract fun medicineDao(): MedicineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicita_db"
                )
                    .fallbackToDestructiveMigration()  // ✅ Esto borrará la DB vieja y creará una nueva
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}