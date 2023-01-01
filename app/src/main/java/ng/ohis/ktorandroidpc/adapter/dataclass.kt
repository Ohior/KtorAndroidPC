package ng.ohis.ktorandroidpc.adapter


import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.explorer.FileUtils
import java.io.File
import java.sql.Date


data class FileModel(
    val file: File
) {
    val path: String get() = file.path
    val sizeInMB: String get() = FileUtils.getStringSize(file.length())
    val extension: String get() = file.extension
    val name: String get() = file.name
    val subFiles: Int get() = file.listFiles()?.size ?: 0
    val isFile: Boolean get() = FileType.FOLDER != fileType
    val fileType: FileType get() = FileType.getFileType(file)
    val staticImage: String
        get() = when (fileType) {
            FileType.FILE -> "/static/file.png"
            FileType.FOLDER -> "/static/folder.png"
            FileType.AUDIO -> "/static/audio.png"
            FileType.IMAGE -> "/static/image.png"
            FileType.VIDEO -> "/static/video.png"
        }
    val drawable: Int
        get() = when (fileType) {
            FileType.FILE -> R.drawable.file
            FileType.FOLDER -> R.drawable.folder
            FileType.AUDIO -> R.drawable.audio
            FileType.IMAGE -> R.drawable.image
            FileType.VIDEO -> R.drawable.video
        }
}

data class NavigateRecyclerAdapterDataclass(
    val name: String
)

data class RecyclerAdapterDataclass(
    val fileModel: FileModel
)

data class StorageDataClass(
    val rootDirectory: String,
    val isSdStorage: Boolean
) {
    fun toJson(): String {
        return """{"rootDirectory": "$rootDirectory","isSdStorage": "$isSdStorage"}""".trimIndent().trim()
    }
}


data class MenuDetailDataClass(val fileModel: FileModel) {
    override fun toString(): String {
        val lastData = Date(fileModel.file.lastModified())
        return "Name : ${fileModel.name}\n" +
                "Modified : $lastData\n" +
                if (fileModel.isFile) {
                    "Size : ${fileModel.sizeInMB}\n" +
                            "Extension : ${fileModel.extension.uppercase()}"
                } else {
                    "Sub-Files : ${fileModel.subFiles}\n"
                }
    }
}