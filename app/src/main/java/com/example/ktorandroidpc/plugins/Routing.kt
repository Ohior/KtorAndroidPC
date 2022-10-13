package com.example.ktorandroidpc.plugins

import android.content.Context
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.utills.FolderDataClass
import com.example.ktorandroidpc.utills.Tools
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

var folders = arrayListOf(
    FolderDataClass(name = "movies"),
    FolderDataClass(name = "pictures"),
    FolderDataClass(name = "music")
)

fun configureRouting(application: Application, context: Context? = null) {
    application.routing {
        get("/") {
//            call.respondText(Tools.readTextFile(context, R.raw.index))
            call.respondRedirect("home")
        }
        route("home") {
            get {
                call.respond(FreeMarkerContent("index.ftl", mapOf("folders" to folders)))
            }
        }
        static("/static") {
            resources("files")
        }
    }
}