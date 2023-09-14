package com.teobaranga.monica.data.photo

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query(
        value = """
        SELECT * FROM photos
        WHERE contactId = :contactId
    """,
    )
    fun getPhotos(contactId: Int): Flow<List<PhotoEntity>>

    @Upsert
    suspend fun upsertPhotos(entities: List<PhotoEntity>)
}
