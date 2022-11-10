package com.example.ktorandroidpc.utills

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
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

    fun getExternalSDCardRootDirectory(activity: Activity): String? {
        if (Tools.isExternalStorageAvailable() || Tools.isExternalStorageReadOnly()) {
            val storageManager = activity.getSystemService(Context.STORAGE_SERVICE)
            try {
                val storageVolume = Class.forName("android.os.storage.StorageVolume")
                val volumeList = storageManager.javaClass.getMethod("getVolumeList")
                val path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    storageVolume.getMethod("getDirectory")
                } else {
                    storageVolume.getMethod("getPath")
                }
                val isRemovable = storageVolume.getMethod("isRemovable")
                val result = volumeList.invoke(storageManager) as Array<*>
                result.forEach {
                    if (isRemovable.invoke(it) as Boolean) {
                        return when (val invokeRequest = path.invoke(it)) {
                            is File -> invokeRequest.absolutePath
                            is String -> invokeRequest
                            else -> null
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return null
        }
        return null
    }

}
