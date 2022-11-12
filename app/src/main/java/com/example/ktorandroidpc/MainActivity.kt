package com.example.ktorandroidpc


import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ktorandroidpc.utills.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Method

//This activity is the host for explorer fragment and connect pc fragment
class MainActivity : AppCompatActivity() {

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Const.PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            return
        } else {
            Tools.requestForAllPermission(this@MainActivity)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        baseContext.cacheDir.deleteRecursively()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SettingsActivity.appSettings(this)
//        display custom actionbar
        setSupportActionBar(findViewById(R.id.id_toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
//        make sure menu is displaying icons
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val m: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible", java.lang.Boolean.TYPE
                )
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return super.onMenuOpened(featureId, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        this holds general menu item
        return when (item.itemId) {
            R.id.id_menu_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }
}
