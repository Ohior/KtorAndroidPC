package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.utills.Const
import com.example.ktorandroidpc.utills.DataManager
import com.example.ktorandroidpc.utills.FileModel
import com.example.ktorandroidpc.utills.Tools
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

data class TemplateData(
    var dirFiles: List<FileModel>,
    var sdDirFiles: List<FileModel>? = null,
    var ifSdCard: Boolean = sdDirFiles != null,
    var showSDCard: Boolean = false,
    var sdCardActive: Boolean = false
)

var directoryFiles: TemplateData? = null

private val sdDirFiles = DataManager.getString(Const.SD_DIRECTORY_KEY)?.let { Tools.getPathFolder(it) }


fun Application.configureRouting() {
    routing {

        navigateHomeDirectory()

        navigateForward()

        navigateBackward()

        uploadFile()

        downloadFile()

        navigateSDHomeDirectory()

        navigateSDForward()

        navigateSDBackward()

        downloadSDFile()

        static("/static") {
            resources("files")
        }
    }
}

private fun Route.navigateHomeDirectory() {
    get("/") {
        // get home page directory
        directoryFiles = TemplateData(
            dirFiles = Tools.getRootFolder().sortedWith(compareBy { it.name }),
            sdDirFiles = sdDirFiles,
            sdCardActive = false
        )
        call.respondRedirect("web")
    }

    route("web") {
        get {
            // load home page
            call.respond(
                MustacheContent(
                    "index.hbs",
                    mapOf("directoryFiles" to directoryFiles)
                )
            )
        }
    }

    get("web/root") {
        // redirect to homepage
        call.respondRedirect("/")
    }
}

private fun Route.navigateBackward() {
    get("web/back") {
        val dirPath = directoryFiles!!
            .dirFiles[0]
            .path
            .split("/")
            .dropLastWhile { it != "0" }
            .joinToString("/")
        // navigate into the folder and get directories
        val dirFiles = Tools.getPathFolder(dirPath).sortedWith(compareBy { it.name })
        // update directory files
        directoryFiles = TemplateData(
            dirFiles = dirFiles,
            sdDirFiles = sdDirFiles
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

private fun Route.navigateForward() {
    get("web/{name}") {
        // get folder name
        // navigate forward into folder
        val name = call.parameters["name"]
        // make sure folder name is valid
        if (name != null) {
            // navigate into the folder and get directories
            val dirFiles = Tools.getDirectoryFromPath("/$name")
            // update directory files
            directoryFiles = TemplateData(
                dirFiles = dirFiles,
                sdDirFiles = sdDirFiles
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

private fun Route.uploadFile() {
    post("upload") {
        try {
            // get the data been pass the server by the form
            val multipartData = call.receiveMultipart()
            multipartData.forEachPart { part ->
                // check which data part is
                when (part) {
                    is PartData.FileItem -> {
                        // upload the data
                        val fileBytes = part.streamProvider().readBytes()
                        val mFile = File(Const.OH_TRANSFER_PATH + part.originalFileName)
                        mFile.writeBytes(fileBytes)
                    }
                    else -> Unit
                }
            }
            call.respondRedirect("web")
        } catch (e: Exception) {
            call.respondRedirect("web")
        }
    }
}

private fun Route.downloadFile() {
    get("download/{name}") {
        //get data been pass to the server
        val name = call.parameters["name"]
        // get the File model from the list
        val getFile = directoryFiles!!.dirFiles.find { it.name == name }
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
