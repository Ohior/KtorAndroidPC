package ng.ohis.ktorandroidpc.utills

import android.Manifest
import android.os.Environment

object Const {
    const val APP_NAME = "Chransver"
    const val FRAGMENT_DATA_KEY: String = "FRAGMENT_DATA_KEY"
    const val SD_DIRECTORY_KEY = "SD_DIRECTORY_KEY"
    const val ADDRESS = "192.168.43.1"
    const val PORT = 8181
    const val PERMISSION_CODE = 101
    const val PREFERENCES_FILE_NAME = "PREFERENCES_FILE_NAME"
    val ROOT_PATH: String by lazy {
        Environment.getExternalStorageDirectory().absolutePath
    }
    val CHRANSVER_DIR by lazy { "$ROOT_PATH/Chransver/" }
    val DOWNLOAD_DIR by lazy { "$ROOT_PATH/Download/" }
    val STORAGE_PERMISSION by lazy {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
    var SETTING_UPLOAD_PATH = CHRANSVER_DIR
    var SETTING_SHOW_HIDDEN_FILES = true

    const val FINE_LOCATION_PERMISSION =  Manifest.permission.ACCESS_FINE_LOCATION
}
