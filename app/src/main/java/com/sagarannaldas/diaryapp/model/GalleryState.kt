package com.sagarannaldas.diaryapp.model

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
fun rememberGalleryState(): GalleryState {
    return remember{ GalleryState() }
}
class GalleryState {
    val images = mutableStateListOf<GalleryImage>()
    val imagesToBeDeleted = mutableStateListOf<GalleryImage>()

    fun addImage(galleryImage: GalleryImage) {
        images.add(galleryImage)
    }

    fun removeImage(galleryImage: GalleryImage) {
        images.remove(galleryImage)
        imagesToBeDeleted.add(galleryImage)
    }

    fun clearImagesToBeDeleted() {
        imagesToBeDeleted.clear()
    }
}

/**
 * A class that represents single image within a gallery
 * @param image The image uri inside a gallery
 * @param remoteImagePath The path of [image] where you plan to upload it.
 */
data class GalleryImage(
    val image: Uri,
    val remoteImagePath: String = ""
)