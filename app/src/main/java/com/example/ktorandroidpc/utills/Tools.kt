package com.example.ktorandroidpc.utills

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
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

    fun openPath(path: String): String {
        return Environment.getExternalStorageDirectory().path + path
    }

    fun showToast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    fun debugMessage(message: String, tag: String = "DEBUG-MESSAGE") {
        Log.e(tag, message)
    }

    fun readTextFile(context: Context?, r: Int): String {
        return context!!.resources.openRawResource(r).bufferedReader()
            .use { it.readText() }
    }

    fun requestForAllPermission(appCompatActivity: AppCompatActivity) {
        unGrantedPermission.clear()
        for (per in Const.ARRAY_OF_PERMISSIONS) {
            if (!checkForPermission(appCompatActivity, per)) {
                unGrantedPermission.add(per)
            }
        }
        if (unGrantedPermission.isNotEmpty()) {
            requestForPermission(appCompatActivity, unGrantedPermission)
        }
    }

    fun checkAllPermission(appCompatActivity: AppCompatActivity): Boolean {
        for (per in Const.ARRAY_OF_PERMISSIONS) {
            if (!checkForPermission(appCompatActivity, per)) {
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

    private fun requestForPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            Const.ARRAY_OF_PERMISSIONS,
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

    fun getDirectoryFromPath(path: String): List<FileModel> {
        directoryPath += path
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                directoryPath,
                showHiddenFiles = true
            )
        )
    }

    fun createDirectoryIfNonExist(dirName: String = Const.OH_TRANSFER_PATH) {
        val file = File(dirName)
        if (!file.exists())file.mkdir()
    }
}
