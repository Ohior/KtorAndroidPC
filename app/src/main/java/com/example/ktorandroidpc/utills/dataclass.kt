package com.example.ktorandroidpc.utills


import com.example.ktorandroidpc.explorer.FileType


data class FileModel(
    val path: String,
    val fileType: FileType,
    val name: String,
    val sizeInMB: Double,
    val extension: String = "",
    val subFiles: Int = 0,
    val isFile: Boolean = FileType.FOLDER != fileType,
    var staticImage: String = when (fileType) {
        FileType.FILE -> "/static/file.png"
        FileType.FOLDER -> "/static/folder.png"
        FileType.AUDIO -> "/static/audio.png"
        FileType.IMAGE -> "/static/image.png"
        FileType.VIDEO -> "/static/video.png"
    }
){
    val fileFolder get() = path.split("/").let { it.elementAt(it.lastIndex-1) }
}


data class RecyclerAdapterDataclass(
    val name: String,
    val detail: String,
    val drawable: Int? = null,
    var fileType: FileType? = null
)

data class ProgressDataClass(
    val dataSize: Int,
    val dataName:String,
    val dataPath:String
)
