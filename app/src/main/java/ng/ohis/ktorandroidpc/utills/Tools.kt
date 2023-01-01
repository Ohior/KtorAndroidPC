package ng.ohis.ktorandroidpc.utills

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ng.ohis.ktorandroidpc.BuildConfig
import ng.ohis.ktorandroidpc.adapter.FileModel
import ng.ohis.ktorandroidpc.adapter.StorageDataClass
import ng.ohis.ktorandroidpc.explorer.FileUtils
import java.io.File
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.ArrayList


object Tools {
    private val directoryPath = Const.ROOT_PATH
    private var unGrantedPermission = ArrayList<String>()


    fun showToast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    fun debugMessage(message: String, tag: String = "DEBUG-MESSAGE") {
        Log.e(tag, message)
    }

    fun requestForAllPermission(activity: Activity) {
        unGrantedPermission.clear()
        for (per in Const.STORAGE_PERMISSION) {
            if (!checkForPermission(activity, per)) {
                unGrantedPermission.add(per)
            }
        }
        if (unGrantedPermission.isNotEmpty()) {
            requestForPermission(activity, unGrantedPermission)
        }
    }

    fun checkAllPermission(activity: Activity): Boolean {
        for (per in Const.STORAGE_PERMISSION) {
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
            Const.PERMISSION_CODE
        )
    }

    private fun checkForPermission(context: Context, permission: String): Boolean {
        val result = ContextCompat.checkSelfPermission(context, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun getFilesFromPath(path: String): List<FileModel> {
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                path
            )
        )
    }

    fun getRootFolder(): List<FileModel> {
//        directoryPath = Const.ROOT_PATH
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                directoryPath
            )
        )
    }

    fun createDirectoryIfNonExist(dirName: String = Const.SETTING_UPLOAD_PATH) {
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
        //        get the root directory of sd card if there is any, return null otherwise
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

    fun deleteFileFromStorage(file: File, context: Context, function: (IntentSender) -> Unit): Uri {
//        val uri = Uri.fromFile(file)
        val uri =
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
        try {
            File(file.absolutePath).deleteRecursively()
//            context?.contentResolver?.delete(uri, null, null)
        } catch (e: SecurityException) {
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(
                        context.contentResolver!!,
                        listOf(uri)
                    ).intentSender
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

    fun navigateFragmentToFragment(fragmentView: Fragment, id: Int) {
        fragmentView.findNavController().navigate(id)
//        Navigation.findNavController(fragmentView).navigate(id)
    }

    fun navigateToFragment(
        fragment: Fragment,
        fragId: Int,
        storageKey: String = Const.FRAGMENT_DATA_KEY,
        storageDataJson: String = StorageDataClass(
            rootDirectory = Const.ROOT_PATH,
            isSdStorage = true
        ).toJson()
    ) {
        fragment.findNavController().navigate(fragId, Bundle().apply {
            putString(storageKey, storageDataJson)
        })
    }

    fun getRandomUUID() = UUID.randomUUID().toString()

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) return locationManager.isLocationEnabled
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun askForPermission(activity: Activity, permissionString: String) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permissionString),
            Const.PERMISSION_CODE
        )
    }

    fun isPermissionGranted(context: Context, permissionString: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permissionString
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isHotspotOn(activity: Activity): Boolean {
        // heck if user hot spot is switch on
        val wifiManager =
            activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method: Method = wifiManager.javaClass.getMethod("getWifiApState")
        method.isAccessible = true
        val invoke = method.invoke(wifiManager) as Int
        return invoke == 13
    }

    fun connectHotspot(activity: Activity) {
//        open intent window so user can activate their mobile hotspot
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val componentName =
            ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.component = componentName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
       activity.startActivity(intent)
    }

}
