package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.MainActivity
import freemarker.cache.ClassTemplateLoader
import freemarker.core.HTMLOutputFormat
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.routing.*

fun MainActivity.configureTemplating(application: Application){
    application.install(FreeMarker){
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
        outputFormat = HTMLOutputFormat.INSTANCE
    }
}