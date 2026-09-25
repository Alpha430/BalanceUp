package com.alpha.balanceup.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alpha.balanceup.data.local.dao.UserDao
import com.alpha.balanceup.data.local.dao.ExpenseDao
import com.alpha.balanceup.data.local.entity.UserEntity
import com.alpha.balanceup.data.local.entity.GroupEntity
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity

@Database(
    entities = [UserEntity::class, GroupEntity::class, ExpenseItemEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "balanceup_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}