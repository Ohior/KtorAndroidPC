package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.utills.Const
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
import kotlinx.coroutines.*
import java.io.File

data class TemplateData(var dirFiles: List<FileModel>, var function: (() -> Unit)?)

private val coroutineScope = CoroutineScope(Dispatchers.IO)

val directoryFiles = TemplateData(
    dirFiles = Tools.getDirectoryFromPath("").sortedWith(compareBy { it.name }),
    function = null
)


fun Application.configureRouting() {
    routing {

        navigateRoute()

        uploadFile()

        downloadFile()

        static("/static") {
            resources("files")
        }
    }
}

fun Route.navigateRoute() {
    get("/") {
        call.respondRedirect("web")
    }

    route("web") {
        get {
            call.respond(
                MustacheContent(
                    "index.hbs",
                    mapOf("directoryFiles" to directoryFiles)
                )
            )
        }
    }
}

fun Route.uploadFile() {
    post("upload") {
        // get the data been pass the server by the form
        val multipartData = call.receiveMultipart()
        multipartData.forEachPart { part ->
            // check which data part is
            when (part) {
                is PartData.FileItem -> {
                    // upload the data
                    val fileBytes = part.streamProvider().readBytes()
                    val mFile = File(Const.OH_TRANSFER_PATH + part.originalFileName)
                    Tools.debugMessage(mFile.absoluteFile.toString(), "NAME")
                    mFile.writeBytes(fileBytes)
                }
                else -> Unit
            }
        }
        call.respondRedirect("web")
    }
}

fun Route.downloadFile(){
    get("download/{name}"){
        //get data been pass to the server
        val name = call.parameters["name"]
        // get the File model from the list
        val getFile = directoryFiles.dirFiles.find { it.name == name }
        // check if the file exits
        if (getFile != null && File(getFile.path).exists()){
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
