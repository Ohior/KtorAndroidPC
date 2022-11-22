package ng.ohis.ktorandroidpc.explorer

import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.FileModel
import java.io.File

object FileUtils {
    fun getFilesFromPath(path: String,onlyFolders: Boolean = false): List<File> {
        val file = File(path)
        return if (file.listFiles() != null) {
            file.listFiles()!!
                .filter { Const.SETTING_SHOW_HIDDEN_FILES || !it.name.startsWith(".") }
                .filter { !onlyFolders || it.isDirectory }
                .toList()
        } else emptyList()
//                .filter { !it.name.contains("%") }
    }

    fun getFileModelsFromFiles(files: List<File>?): List<FileModel> {
        return files!!.map {
            FileModel(
                path = it.path,
                fileType = FileType.getFileType(it),
                name = it.name,
                sizeInMB = convertFileSizeToMB(it.length()),
                extension = it.extension,
                subFiles = it.listFiles()?.size ?: 0,
            )
        }
    }

    fun convertFileSizeToMB(sizeInBytes: Long): Double {
        return (sizeInBytes.toDouble()) / (1024 * 1024)
    }
}
