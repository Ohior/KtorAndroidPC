package com.example.ktorandroidpc.explorer

import com.example.ktorandroidpc.utills.FileModel
import java.io.File

object FileUtils {
    fun getFilesFromPath(path: String, showHiddenFiles: Boolean = false, onlyFolders: Boolean = false): List<File> {
        val file = File(path)
        return file.listFiles()!!
            .filter { showHiddenFiles || !it.name.startsWith(".") }
            .filter { !onlyFolders || it.isDirectory }
            .toList()
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