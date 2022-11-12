package ng.ohis.ktorandroidpc.utills

import android.app.Activity
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ng.ohis.ktorandroidpc.BuildConfig
import ng.ohis.ktorandroidpc.explorer.FileUtils
import ng.ohis.ktorandroidpc.utills.FileModel
import java.io.File


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

    fun createDirectoryIfNonExist(dirName: String = Const.UPLOAD_PATH) {
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
        if (isExternalStorageAvailable() || isExternalStorageReadOnly()) {
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

    fun popUpWindow(
        context: Context,
        message: String,
        title: String = "",
        lambda: ((AlertDialog.Builder) -> Unit)? = null
    ) {
        AlertDialog.Builder(context).apply {
            this.setCancelable(false)
            this.setTitle(title)
            this.setMessage(message)
            lambda!!(this)
        }.show()
    }

    fun deleteFileFromStorage(file: File, context: Context, function: (IntentSender)->Unit): Uri {
//        val uri = Uri.fromFile(file)
        val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        try {
            File(file.absolutePath).deleteRecursively()
//            context?.contentResolver?.delete(uri, null, null)
        } catch (e: SecurityException) {
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(context.contentResolver!!, listOf(uri)).intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val recoverableSecurityException = e as RecoverableSecurityException
                    recoverableSecurityException.userAction.actionIntent.intentSender
                }
                else -> null
            }
            intentSender?.let { function(it) }
        }
        return uri
    }


}
