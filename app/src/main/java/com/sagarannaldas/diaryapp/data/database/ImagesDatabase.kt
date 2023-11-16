package com.sagarannaldas.diaryapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sagarannaldas.diaryapp.data.database.entity.ImageToDelete
import com.sagarannaldas.diaryapp.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 1,
    exportSchema = false

)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun imagesToUploadDao(): ImagesToUploadDao
    abstract fun imagesToDeleteDao(): ImagesToDeleteDao
}