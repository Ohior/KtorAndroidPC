package com.example.ktorandroidpc.explorer

import java.io.File


private val audioExtensions = listOf("mp3", "ogg")
private val videoExtensions = listOf("mp4", "mkv")
private val imageExtensions = listOf("png", "jpg")

enum class FileType {
    FILE,
    FOLDER,
    AUDIO,
    IMAGE,
    VIDEO;

    companion object {
        fun getFileType(file: File) = when (true) {
            file.isDirectory -> FOLDER
            videoExtensions.contains(file.extension) -> VIDEO
            audioExtensions.contains(file.extension) -> AUDIO
            imageExtensions.contains(file.extension) -> IMAGE
            else -> FILE
        }
    }
}