package com.audio.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface MusicDAO {
    @Insert
    suspend fun insertMusic(music: Music)

    @Query("SELECT * FROM music")
    suspend fun getAllMusic() : List<Music>

    @Query("SELECT * FROM music WHERE path = :path LIMIT 1")
    suspend fun getMusicByPath(path: String): Music?
    

    @Query("SELECT * FROM music WHERE id = :id")
    suspend fun selectMusicById(id: Int) : Music

    @Update
    suspend fun updateMusic(music: Music)

    @Query("DELETE FROM music WHERE path = :path")
    suspend fun deleteMusicByPath(path: String)

    @Transaction
    suspend fun upsertMusic(music: Music) {
        val existing = getMusicByPath(music.path)
        if (existing == null) {
            insertMusic(music)
        } else {
            val updated = music.copy(id = existing.id, isFavorite = existing.isFavorite)
            updateMusic(updated)
        }
    }
}