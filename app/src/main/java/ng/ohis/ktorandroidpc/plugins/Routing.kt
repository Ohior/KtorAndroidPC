package ng.ohis.ktorandroidpc.plugins


import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.DataManager
import ng.ohis.ktorandroidpc.utills.FileModel
import ng.ohis.ktorandroidpc.utills.Tools
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.mustache.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


data class TemplateData(
    var dirFiles: List<FileModel>,
    val ifSdCard: Boolean = Tools.isExternalStorageReadOnly() || Tools.isExternalStorageAvailable(),
)

var templateData: TemplateData? = null

private val homeDirectoryPath = Const.ROOT_PATH

private var directoryPath = Const.ROOT_PATH

private var rootDirectory = homeDirectoryPath

private val sdDirectoryPath = DataManager.getString(Const.SD_DIRECTORY_KEY)

fun Application.configureRouting(function: (Route)->Unit) {
    routing {
        navigateHomeDirectory()

        navigateForward()

        navigateBackward()

        navigateSDHomeDirectory()

        downloadFile()

        function(this)

        static("/static") {
            resources("files")
        }
    }
}

private fun Route.navigateSDHomeDirectory() {
    get("web/sd-dir") {
        // Reset root directory to SD card
        directoryPath = sdDirectoryPath!!
        rootDirectory = sdDirectoryPath
        // initialize the template data
        templateData = TemplateData(
            dirFiles = Tools.getFilesFromPath(directoryPath).sortedWith(compareBy { it.name }),
        )
        // load home page
        call.respond(
            MustacheContent(
                "index.hbs",
                mapOf("templateData" to templateData)
            )
        )
    }
}

private fun Route.navigateHomeDirectory() {
    get("/") {
        directoryPath = homeDirectoryPath
        // Reset root directory to SD card
        rootDirectory = homeDirectoryPath
        // initialize the template data
        templateData = TemplateData(
            dirFiles = Tools.getRootFolder().sortedWith(compareBy { it.name }),
        )
        call.respondRedirect("web")
    }

    route("web") {
        get {
            // load home page
            call.respond(
                MustacheContent(
                    "index.hbs",
                    mapOf("templateData" to templateData)
                )
            )
        }
    }

    get("web/root") {
        // redirect to homepage
        call.respondRedirect("/")
    }
}

private fun Route.navigateForward() {
    get("web/{name}") {
        // get folder name
        // navigate forward into folder
        val name = call.parameters["name"]
        // make sure folder name is valid
        if (name != null) {
            directoryPath += "/$name"
            // navigate into the folder and get directories
            val dirFiles = Tools.getFilesFromPath(directoryPath)
            // update directory files
            templateData = TemplateData(
                dirFiles = dirFiles,
            )
            // open page
            call.respond(
                MustacheContent(
                    "index.hbs",
                    mapOf("templateData" to templateData)
                )
            )
        }
    }
}

private fun Route.navigateBackward() {
    get("web/back") {
        val dirPath = directoryPath
            .split("/")

        if (dirPath.last() != rootDirectory.split("/").last()) {
            directoryPath = dirPath.dropLast(1).joinToString("/")
        }
        // navigate into the folder and get directories
        val dirFiles = Tools.getFilesFromPath(directoryPath).sortedWith(compareBy { it.name })
        // update directory files
        templateData = TemplateData(
            dirFiles = dirFiles,
        )
        // open page
        call.respond(
            MustacheContent(
                "index.hbs",
                mapOf("templateData" to templateData)
            )
        )
    }
}

private fun Route.downloadFile() {
    get("download/{name}") {
        //get data been pass to the server
        val name = call.parameters["name"]
        // get the File model from the list
        val getFile = templateData!!.dirFiles.find { it.name == name }
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
        call.respondRedirect("web")
    }
}

fun Route.uploadFile(function: (File) -> Unit) {
    post("upload") {
        try {

//            // get the data been pass the server by the form
        val multipartData = call.receiveMultipart()
        withContext(Dispatchers.IO) {
            withContext(Dispatchers.IO) {
                multipartData.forEachPart { part ->
                    // check which data part is
                    when (part) {
                        is PartData.FileItem -> {
                            // upload the data
                            val inputStream = part.streamProvider()
                            val fileBytes = inputStream.readBytes()
                            val mFile = File(Const.UPLOAD_PATH + part.originalFileName)
                            mFile.writeBytes(fileBytes)
                            function(mFile)
                        }
                        else -> Unit
                    }
                    call.respondRedirect("web")
                    part.dispose()
                }
            }
        }
        }catch (e:Exception){
        call.respondRedirect("web")

        }

    }
}

fun Route.uploadFile(name: String) {
    post("upload") {
        try {
            // get the data been pass the server by the form
            val multipartData = call.receiveMultipart()

            multipartData.forEachPart { part ->
                // check which data part is
                when (part) {
                    is PartData.FileItem -> {
                        // upload the data
                        val inputStream = part.streamProvider()
                        val fileBytes = inputStream.readBytes()
                        val mFile = File(Const.UPLOAD_PATH + part.originalFileName)
                        mFile.writeBytes(fileBytes)
//                        val mFile = File(Const.OH_UPLOAD_PATH + part.originalFileName)
//                        part.streamProvider().use { inputStream ->
//                            mFile.outputStream().buffered().use {
//                                inputStream.copyTo(it)
//                            }
//                        }
                    }
                    else -> Unit
                }
                call.respondRedirect("web")
                part.dispose()
            }
//            call.respondRedirect("web")
        } catch (e: Exception) {
            call.respondRedirect("web")
        }
    }
}

