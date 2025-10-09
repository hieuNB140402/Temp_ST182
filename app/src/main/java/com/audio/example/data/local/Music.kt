package com.audio.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music")
data class Music(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val path: String = "",
    val songName: String,
    val singerName: String,
    val imageThumb: String,
    val duration: Long,
    var isFavorite: Boolean = false,
    var isPlaying: Boolean = false,
)
