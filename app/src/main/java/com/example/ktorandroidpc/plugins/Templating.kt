package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.MainActivity
import com.github.mustachejava.DefaultMustacheFactory
import freemarker.cache.ClassTemplateLoader
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.mustache.*


fun MainActivity.configureTemplating(application: Application) {
    application.install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }
    application.install(FreeMarker) {
        templateLoader = ClassTemplateLoader(application.environment.classLoader, "templates")
    }
//    }
}