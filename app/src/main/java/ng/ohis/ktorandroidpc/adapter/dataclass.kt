package ng.ohis.ktorandroidpc.utills


import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.explorer.FileType
import java.io.File


data class FileModel(
    val name: String,
    val path: String,
    val fileType: FileType,
    val sizeInMB: Double,
    val extension: String = "",
    val subFiles: Int = 0,
) {
    val file: File get() = File(path)
    val isFile: Boolean get() = FileType.FOLDER != fileType
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
    val fileFolder: String get() = path.split("/").let { it.elementAt(it.lastIndex - 1) }
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


data class SettingsDataClass(
    val downloadFolder:String,
    val showHiddenFiles:Boolean
){
    fun toJson() = """{"downloadFolder":"$downloadFolder", "showHiddenFiles":"$showHiddenFiles"}""".trim()
}