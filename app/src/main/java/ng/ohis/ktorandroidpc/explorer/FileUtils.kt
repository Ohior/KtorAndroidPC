package ng.ohis.ktorandroidpc.explorer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ng.ohis.ktorandroidpc.utills.FileModel
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object FileUtils {
    fun getFilesFromPath(path: String, showHiddenFiles: Boolean = false, onlyFolders: Boolean = false): List<File> {
        val file = File(path)
        return if (file.listFiles() != null) {
            file.listFiles()!!
                .filter { showHiddenFiles || !it.name.startsWith(".") }
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
