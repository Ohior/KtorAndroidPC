package com.example.ktorandroidpc.utills

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ktorandroidpc.MainActivity
import com.example.ktorandroidpc.explorer.FileUtils
import java.io.InputStream


object Tools {
    private val mActivity = MainActivity()
    private var directoryPath = Environment.getExternalStorageDirectory().absolutePath

    fun OpenPath(path: String): String {
        return Environment.getExternalStorageDirectory().path+path
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

    private fun requestForPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            Const.PERMISSION
        )
    }

    fun checkForReadExternalStoragePermission(context: Context): Boolean {
        val result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun requestForPermissions(context: Context, activity: Activity): Boolean {
        return if (checkForReadExternalStoragePermission(context)) {
            true
        } else {
            requestForPermission(activity)
            false
        }
    }

    fun getDrawableUri(drawable: Int): InputStream? {
        val uri = Uri.parse("android.resource://" + mActivity.packageName + "/" + drawable)
        return mActivity.contentResolver.openInputStream(uri)
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
}
