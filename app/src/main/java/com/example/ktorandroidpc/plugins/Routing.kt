package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.utills.FileModel
import com.example.ktorandroidpc.utills.Tools
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

data class TemplateData(var dirFiles: List<FileModel>, var function: (() -> Unit)?)

private lateinit var DirFiles: List<FileModel>
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("web")
        }

        route("web") {
            get {
                val directoryFiles = TemplateData(
                    dirFiles = Tools.getDirectoryFromPath("").sortedWith(compareBy { it.name }),
                    function = null
                )

                DirFiles = directoryFiles.dirFiles
                call.respond(
                    MustacheContent(
                        "index.hbs",
                        mapOf("directoryFiles" to directoryFiles)
                    )
                )
            }

        }

        static("/static") {
            resources("files")
        }
    }
}
