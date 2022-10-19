package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.utills.Const
import com.example.ktorandroidpc.utills.DataManager
import com.example.ktorandroidpc.utills.FileModel
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.collections.ArrayList


data class User(val id: Array<Int>, val name: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (!id.contentEquals(other.id)) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

val directoryFiles = DataManager
    .retrievePreferenceData(Const.ROOT_FOLDER_KEY)

fun Application.configureRouting(application: Application) {
    routing {
        get("/") {
            call.respondRedirect("web")
        }

        route("web") {
            get {
                val user = User(id = arrayOf(1,2,3), name = "Ohis")
                call.respond(MustacheContent("index.hbs", mapOf("user" to directoryFiles)))
            }
        }

        static("/static") {
            resources("files")
        }
    }
}
