package com.example.ktorandroidpc.utills

import android.os.Environment

object Const {
    const val ADDRESS = "192.168.43.1"
    const val PORT = 8181
    const val PERMISSION = 101
    const val AUTHORITY = "com.example.ktorandroidpc.fileprovider"
    const val ROOT_FOLDER_KEY = "ROOT_FOLDER_KEY"
    const val PRESENT_FOLDER_KEY = "PRESENT_FOLDER_KEY"
    const val PREFERENCES_FILE_NAME = "PREFERENCES_FILE_NAME"
    val ROOT_PATH = Environment.getExternalStorageDirectory().absolutePath
    const val OH_TRANSFER = "OH_TRANSFER"

}