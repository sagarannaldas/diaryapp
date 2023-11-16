package com.sagarannaldas.diaryapp.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sagarannaldas.diaryapp.util.Constants.IMAGES_TO_DELETE_TABLE

@Entity(tableName = IMAGES_TO_DELETE_TABLE)
data class ImageToDelete(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteImagePath: String
)