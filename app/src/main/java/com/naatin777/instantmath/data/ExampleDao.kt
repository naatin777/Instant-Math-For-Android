package com.naatin777.instantmath.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExampleDao {
    @Query("SELECT * FROM examples")
    fun getAll(): Flow<List<ExampleEntity>>

    @Insert
    suspend fun insert(entity: ExampleEntity)
}
