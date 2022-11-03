package com.example.ktorandroidpc.utills

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ktorandroidpc.explorer.FileUtils
import java.io.File
import java.io.InputStream


object Tools {
    private var directoryPath = Const.ROOT_PATH
    private var unGrantedPermission = ArrayList<String>()


    fun showToast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    fun debugMessage(message: String, tag: String = "DEBUG-MESSAGE") {
        Log.e(tag, message)
    }

    fun requestForAllPermission(activity: Activity) {
        unGrantedPermission.clear()
        for (per in Const.ARRAY_OF_PERMISSIONS) {
            if (!checkForPermission(activity, per)) {
                unGrantedPermission.add(per)
            }
        }
        if (unGrantedPermission.isNotEmpty()) {
            requestForPermission(activity, unGrantedPermission)
        }
    }

    fun checkAllPermission(activity: Activity): Boolean {
        for (per in Const.ARRAY_OF_PERMISSIONS) {
            if (!checkForPermission(activity, per)) {
                return false
            }
        }
        return true
    }

    private fun requestForPermission(activity: Activity, unGrantedPermission: MutableList<String>) {
        ActivityCompat.requestPermissions(
            activity,
            unGrantedPermission.toTypedArray(),
            Const.PERMISSION
        )
    }

    private fun checkForPermission(context: Context, permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(context, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun getDrawableUri(drawable: Int, appCompatActivity: AppCompatActivity): InputStream? {
        val uri = Uri.parse("android.resource://" + appCompatActivity.packageName + "/" + drawable)
        return appCompatActivity.contentResolver.openInputStream(uri)
    }

    fun getDirectoryFromPath(path: String, showHiddenFiles: Boolean = true): List<FileModel> {
        directoryPath += path
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                directoryPath,
                showHiddenFiles = showHiddenFiles
            )
        )
    }

    fun getFilesFromPath(path: String, showHiddenFiles: Boolean = true): List<FileModel> {
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                path,
                showHiddenFiles = showHiddenFiles
            )
        )
    }

    fun getRootFolder(): List<FileModel> {
        directoryPath = Const.ROOT_PATH
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                directoryPath,
                showHiddenFiles = true
            )
        )
    }

    fun getPathFolder(path: String): List<FileModel> {
        directoryPath = path
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                directoryPath,
                showHiddenFiles = true
            )
        )
    }

    fun createDirectoryIfNonExist(dirName: String = Const.OH_TRANSFER_PATH) {
        val file = File(dirName)
        if (!file.exists()) file.mkdir()
    }

    fun isExternalStorageReadOnly(): Boolean {
        return Environment.MEDIA_MOUNTED_READ_ONLY == Environment.getExternalStorageState()
    }

    fun isExternalStorageAvailable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    fun popUpWindow(
        context: Context,
        title: String,
        layout: Int,
        lambda: ((View, AlertDialog) -> Unit)? = null
    ) {
        val view = LayoutInflater.from(context)
            .inflate(layout, null)
        AlertDialog.Builder(context).apply {
            this.setCancelable(false)
            this.setTitle(title)
            this.setView(view)
            lambda!!(view, this.show())
        }.show()
    }

}
