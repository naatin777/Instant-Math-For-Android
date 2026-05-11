package com.naatin777.instantmath.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExampleEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exampleDao(): ExampleDao
}
