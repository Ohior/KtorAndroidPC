package ng.ohis.ktorandroidpc.utills

import android.Manifest
import android.os.Environment

object Const {
    const val FRAGMENT_DATA_KEY: String = "FRAGMENT_DATA_KEY"
    const val SD_DIRECTORY_KEY = "SD_DIRECTORY_KEY"
    const val ADDRESS = "192.168.43.1"
    const val PORT = 8181
    const val FULL_ADDRESS = "$ADDRESS:$PORT"
    const val PERMISSION = 101
    const val AUTHORITY = "ng.ohis.ktorandroidpc.fileprovider"
    const val ROOT_FOLDER_KEY = "ROOT_FOLDER_KEY"
    const val PRESENT_FOLDER_KEY = "PRESENT_FOLDER_KEY"
    const val PREFERENCES_FILE_NAME = "PREFERENCES_FILE_NAME"
    val ROOT_PATH: String = Environment.getExternalStorageDirectory().absolutePath
    val CHRANSVER_DIR = "$ROOT_PATH/Chransver/"
    val DOWNLOAD_DIR = "$ROOT_PATH/Download/"
    var UPLOAD_PATH = CHRANSVER_DIR
    const val PROGRESS_KEY = "PROGRESS_KEY"
    val ARRAY_OF_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    )
}
