package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.explorer.FileUtils
import com.example.ktorandroidpc.utills.Const
import com.example.ktorandroidpc.utills.DataManager
import com.example.ktorandroidpc.utills.FileModel
import com.example.ktorandroidpc.utills.Tools
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File


val dirFiles = Tools.getRootFolder().sortedWith(compareBy { it.name })

fun Route.navigateSDHomeDirectory() {
    get("web/sd-dir") {
        directoryFiles = TemplateData(
            dirFiles = dirFiles,
            sdDirFiles = SDRouting.getRootFolder().sortedWith(compareBy { it.name }),
            showSDCard = directoryFiles?.ifSdCard == true,
            sdCardActive = true
        )
        // load home page
        call.respond(
            MustacheContent(
                "index.hbs",
                mapOf("directoryFiles" to directoryFiles)
            )
        )
    }
}

fun Route.navigateSDForward() {
    get("web/sd/{name}") {
        // get folder name
        // navigate forward into folder
        val name = call.parameters["name"]
        // make sure folder name is valid
        if (name != null) {
            // navigate into the folder and get directories
            val sdDirFiles = SDRouting.getDirectoryFromPath("/$name").sortedWith(compareBy { it.name })
            // update directory files
            directoryFiles = TemplateData(
                dirFiles = dirFiles,
                sdDirFiles = sdDirFiles,
                showSDCard = directoryFiles?.ifSdCard == true,
                sdCardActive = true
            )
            // open page
            call.respond(
                MustacheContent(
                    "index.hbs",
                    mapOf("directoryFiles" to directoryFiles)
                )
            )
        }
    }
}

fun Route.navigateSDBackward() {
    get("web/sd/back") {
        // get updated sd path
        val dirString = SDRouting.sdDirectoryPath
        // check and make sure path is okay
        if (dirString != null && SDRouting.sdRootDir  != dirString) {
            // get a new path
            val dirStr = dirString.split("/")
                .dropLast(1)
                .joinToString("/")
            // navigate to that path
            val sdDirFiles = SDRouting.getPathFolder(dirStr).sortedWith(compareBy { it.name })
            directoryFiles = TemplateData(
                dirFiles = dirFiles,
                sdDirFiles = sdDirFiles,
                showSDCard = true,
                sdCardActive = true
            )
        }
            // open page
        call.respond(
            MustacheContent(
                "index.hbs",
                mapOf("directoryFiles" to directoryFiles)
            )
        )
    }
}

fun Route.downloadSDFile() {
    get("download/sd/{name}") {
        //get data been pass to the server
        val name = call.parameters["name"]
        // get the File model from the list
        val getFile = directoryFiles!!.sdDirFiles?.find { it.name == name }
        // check if the file exits
        if (getFile != null) {
            val file = File(getFile.path)
            // make the file downloadable and not playable
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(
                    ContentDisposition.Parameters.FileName,
                    getFile.name
                ).toString()
            )
            // actually download the file
            call.respondFile(file)
        }
    }
}


private object SDRouting {

    var sdDirectoryPath = DataManager.getString(Const.SD_DIRECTORY_KEY)
    val sdRootDir = DataManager.getString(Const.SD_DIRECTORY_KEY)

    fun getDirectoryFromPath(path: String, showHiddenFiles: Boolean = true): List<FileModel> {
        sdDirectoryPath += path
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                sdDirectoryPath!!,
                showHiddenFiles = showHiddenFiles
            )
        )
    }

    fun getRootFolder(): List<FileModel> {
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                sdRootDir!!,
                showHiddenFiles = true
            )
        )
    }

    fun getPathFolder(path: String?): List<FileModel> {
        sdDirectoryPath = path
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                sdDirectoryPath!!,
                showHiddenFiles = true
            )
        )
    }
}