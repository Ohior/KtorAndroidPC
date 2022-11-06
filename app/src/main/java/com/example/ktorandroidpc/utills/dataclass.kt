package com.example.ktorandroidpc.utills


import android.graphics.drawable.Drawable
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.explorer.FileType
import java.io.File


data class FileModel(
    val name: String,
    val path: String,
    val fileType: FileType,
    val sizeInMB: Double,
    val extension: String = "",
    val subFiles: Int = 0,
){
    val file: File get() = File(path)
    val isFile: Boolean get() = FileType.FOLDER != fileType
    val staticImage: String get() = when (fileType) {
        FileType.FILE -> "/static/file.png"
        FileType.FOLDER -> "/static/folder.png"
        FileType.AUDIO -> "/static/audio.png"
        FileType.IMAGE -> "/static/image.png"
        FileType.VIDEO -> "/static/video.png"
    }
    val drawable: Int get() = when (fileType) {
        FileType.FILE -> R.drawable.file
        FileType.FOLDER -> R.drawable.folder
        FileType.AUDIO -> R.drawable.audio
        FileType.IMAGE -> R.drawable.image
        FileType.VIDEO -> R.drawable.video
    }
    val fileFolder get() = path.split("/").let { it.elementAt(it.lastIndex-1) }
}


data class RecyclerAdapterDataclass(
    val fileModel: FileModel
)

data class ProgressDataClass(
    val dataSize: Int,
    val dataName:String,
    val dataPath:String
)
