package ng.ohis.ktorandroidpc.plugins

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.server.application.*
import io.ktor.server.mustache.*


fun configureTemplating(application: Application) {
    application.install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates")
    }
}