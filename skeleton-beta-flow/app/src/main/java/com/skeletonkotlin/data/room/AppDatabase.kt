package com.skeletonkotlin.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.skeletonkotlin.data.room.dao.DoorDao
import com.skeletonkotlin.data.room.entity.DoorEntity

@Database(entities = [DoorEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doorDao(): DoorDao
}