package com.example.ktorandroidpc.plugins

import androidx.lifecycle.createSavedStateHandle
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

data class TemplateData(var dirFiles: List<FileModel>, var function: (() -> Unit)?)

private val coroutineScope = CoroutineScope(Dispatchers.IO)

fun Application.configureRouting() {
    routing {

        navigateRoute()


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
            val directoryFiles = TemplateData(
                dirFiles = Tools.getDirectoryFromPath("").sortedWith(compareBy { it.name }),
                function = null
            )
            call.respond(
                MustacheContent(
                    "index.hbs",
                    mapOf("directoryFiles" to directoryFiles)
                )
            )
        }
    }
    uploadFile()
}

fun Route.uploadFile() {
    post("upload") {
        val receivedData = call.receive<ByteArray>()
        Tools.debugMessage(receivedData.size.toString(), "BYTE ARRAY")
        call.respondText("Post working")
    }
}
