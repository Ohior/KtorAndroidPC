package com.example.ktorandroidpc

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.example.ktorandroidpc.databinding.ActivityMainBinding
import com.example.ktorandroidpc.plugins.configureRouting
import com.example.ktorandroidpc.plugins.configureTemplating
import com.example.ktorandroidpc.utills.Const
import com.example.ktorandroidpc.utills.DataManager
import com.example.ktorandroidpc.utills.FileModel
import com.example.ktorandroidpc.utills.Tools
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var coroutineScope: CoroutineScope
    private var connectOrDisconnect = true
    private var sdDirectory: String? = null

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Const.PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            Executional()
        } else {
            Tools.requestForAllPermission(this@MainActivity)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        if (coroutineScope.isActive || ::coroutineScope.isInitialized) {
            coroutineScope.cancel()
        }
        super.onDestroy()
    }

    override fun onPause() {
        if (coroutineScope.isActive || ::coroutineScope.isInitialized) {
            coroutineScope.cancel()
        }
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Tools.requestForAllPermission(this)

        Initiallizers()

        ClickListener()

        if (Tools.checkAllPermission(this)) {
            Executional()
        }

        coroutineScope.launch {
            embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
                configureRouting()
                configureTemplating(this)
            }.start(wait = false)
        }
    }

    private fun Executional() {
        Tools.createDirectoryIfNonExist()
    }

    private fun ClickListener() {
        binding.idBtnConnect2browser.setOnClickListener {
            if (connectOrDisconnect) {
                coroutineScope.launch {
                    embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
                        configureRouting()
                        configureTemplating(this)
                    }.start(wait = false)
                }
            } else {
                coroutineScope.cancel()
                Tools.showToast(application, "Connection Disabled")
            }
            connectOrDisconnect = !connectOrDisconnect

        }

        binding.idBtnConnectWithApplication.setOnClickListener {
            val intent = Intent(this, ExplorerActivity::class.java)
            startActivity(intent)
            coroutineScope.cancel()
        }
    }

    private fun Initiallizers() {
        coroutineScope = CoroutineScope(Dispatchers.IO)
        sdDirectory = if (Tools.isExternalStorageAvailable() || Tools.isExternalStorageReadOnly()) {
            GetExternalSDCardRootDirectory()
        } else null
        DataManager.with(application).setString(Const.SD_DIRECTORY_KEY, sdDirectory)
    }

    private fun GetExternalSDCardRootDirectory(): String? {
        val storageManager = this.getSystemService(Context.STORAGE_SERVICE)
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
                    return when (val invokeRquest = path.invoke(it)) {
                        is File -> invokeRquest.absolutePath
                        is String -> invokeRquest
                        else -> null
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

}
