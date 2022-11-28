package ng.ohis.ktorandroidpc.explorer

import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.adapter.FileModel
import java.io.*
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.jvm.Throws
import kotlin.math.log10
import kotlin.math.pow

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
            FileModel(it)
        }
    }

    fun zipSingleFile(
        file: File,
        zipFileName: String,
        function: ((size: String, count: String, name: String) -> Unit)? = null
    ) {
        try {
            //create ZipOutputStream to write to the zip file
            val fos = FileOutputStream(zipFileName)
            val zos = ZipOutputStream(fos)
            val size = file.length()
            //add a  Zip Entry to the ZipOutputStream
            val ze = ZipEntry(file.name)
            ze.time = file.lastModified()
            zos.putNextEntry(ze)
            //read the file and write to ZipOutputStream
            val fis = FileInputStream(file)
            val buffer = ByteArray(1024)
            var len = fis.read(buffer)
            var count = 0
            while (len > 0) {
                count += len
                zos.write(buffer, 0, len)
                len = fis.read(buffer)
                if (function != null) {
                    function(getStringSize(size), getStringSize(count.toLong()), file.name)
                }
            }

            //Close the zip entry to write to zip file
            zos.closeEntry()
            //Close resources
            zos.close()
            fis.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun zipDirectory(
        dir: File,
        zipDirName: String,
        function: ((size: String, count: String, name: String) -> Unit)? = null
    ) {
        val filesListInDir = ArrayList<String>()
        try {
            populateFilesList(dir, filesListInDir)
            //now zip files one by one
            //create ZipOutputStream to write to the zip file
            val fos = FileOutputStream(zipDirName)
            val zos = ZipOutputStream(fos)
            val size = dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum() // dirSize(dir)
            var count = 0
            for (filePath in filesListInDir) {
                //for ZipEntry we need to keep only relative file path, so we used substring on absolute path
                val name = filePath.substring(dir.absolutePath.length + 1, filePath.length)
                val ze = ZipEntry(name)
                ze.time = dir.lastModified()
                zos.putNextEntry(ze)
                //read the file and write to ZipOutputStream
                val fis = FileInputStream(filePath)
                val buffer = ByteArray(1024)
                var len = fis.read(buffer)
                while (len > 0) {
                    count += len
                    println("Count $count")
                    zos.write(buffer, 0, len)
                    len = fis.read(buffer)
                    if (function != null) {
                        function(getStringSize(size), getStringSize(count.toLong()), name)
                    }
                }
                zos.closeEntry()
                fis.close()
            }
            zos.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun populateFilesList(dir: File, filesListInDir: ArrayList<String>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isFile) filesListInDir.add(file.absolutePath)
            else populateFilesList(file, filesListInDir)
        }
    }

    private fun dirSize(dir: File): Long {
        if (dir.exists()) {
            var result: Long = 0
            val fileList = dir.listFiles()
            for (i in fileList!!.indices) {
                result += if (fileList[i].isDirectory) {
                    dirSize(fileList[i])
                } else {
                    fileList[i].length()
                }
            }
            return result
        }
        return 0
    }

    fun getStringSize(size: Long): String {
        if (size <= 0)
            return "0MB"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
