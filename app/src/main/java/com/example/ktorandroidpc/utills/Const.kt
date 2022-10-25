package com.example.ktorandroidpc.utills

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.ktorandroidpc.MainActivity
import io.ktor.server.application.*
import java.io.File

object Const {
    const val SD_DIRECTORY_KEY = "SD_DIRECTORY_KEY"
    const val ADDRESS = "192.168.43.1"
    const val PORT = 8181
    const val PERMISSION = 101
    const val AUTHORITY = "com.example.ktorandroidpc.fileprovider"
    const val ROOT_FOLDER_KEY = "ROOT_FOLDER_KEY"
    const val PRESENT_FOLDER_KEY = "PRESENT_FOLDER_KEY"
    const val PREFERENCES_FILE_NAME = "PREFERENCES_FILE_NAME"
    val ROOT_PATH: String = Environment.getExternalStorageDirectory().absolutePath
    private const val OH_TRANSFER = "OH-Transfer"
    val OH_TRANSFER_PATH = "$ROOT_PATH/$OH_TRANSFER/"
    val ARRAY_OF_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
}
