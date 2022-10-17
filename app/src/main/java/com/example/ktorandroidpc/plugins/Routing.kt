package com.example.ktorandroidpc.plugins

import android.content.Context
import com.example.ktorandroidpc.MainActivity
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.utills.Const
import com.example.ktorandroidpc.utills.DataManager
import com.example.ktorandroidpc.utills.FolderDataClass
import com.example.ktorandroidpc.utills.Tools
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

val dir = DataManager.retrievePreferenceData(Const.ROOT_FOLDER_KEY)

fun MainActivity.configureRouting(application: Application, context: Context? = null) {
    application.routing {

        static("/static") {
            resources("files")
        }
    }
}