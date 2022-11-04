package com.example.ktorandroidpc.plugins

import com.example.ktorandroidpc.utills.Tools
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*


suspend fun getUploadDownloadProgress() {
//    val client = HttpClient(Android)
    val client = HttpClient(CIO)
    val httpResponse: HttpResponse = client.get("http://192.168.43.1:8181/") {
        onDownload() { bytesSentTotal, contentLength ->
            Tools.debugMessage("bytesSentTotal $bytesSentTotal contentLength $contentLength", "DOWNLOAD")
        }
        onUpload { bytesSentTotal, contentLength ->
            Tools.debugMessage("bytesSentTotal $bytesSentTotal contentLength $contentLength", "UPLOAD")
        }
    }
    Tools.debugMessage(httpResponse.status.description)
}