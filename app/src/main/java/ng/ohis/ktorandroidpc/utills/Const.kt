package ng.ohis.ktorandroidpc.utills

import android.Manifest
import android.os.Environment

object Const {
    const val FRAGMENT_DATA_KEY: String = "FRAGMENT_DATA_KEY"
    const val SD_DIRECTORY_KEY = "SD_DIRECTORY_KEY"
    const val ADDRESS = "192.168.43.1"
    const val PORT = 8181
    const val PERMISSION = 101
    const val SETTINGS_KEY = "SETTINGS_KEY"
    const val PREFERENCES_FILE_NAME = "PREFERENCES_FILE_NAME"
    val ROOT_PATH: String by lazy {
        Environment.getExternalStorageDirectory().absolutePath
    }
    val CHRANSVER_DIR by lazy { "$ROOT_PATH/Chransver/" }
    val DOWNLOAD_DIR by lazy { "$ROOT_PATH/Download/" }
    val ARRAY_OF_PERMISSIONS by lazy {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
    var SETTING_UPLOAD_PATH = CHRANSVER_DIR
    var SETTING_SHOW_HIDDEN_FILES = true
    var FRAGMENT_TAG = ""
    const val FULL_ADDRESS = "$ADDRESS:$PORT"
    const val REQUEST_CODE_OPEN_DOCUMENT_TREE = 102
    const val AUTHORITY = "ng.ohis.ktorandroidpc.fileprovider"

    //    Manifest.permission.ACCESS_FINE_LOCATION
}
