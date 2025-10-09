package com.audio.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [Music::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase(){
    abstract fun musicDao(): MusicDAO

}