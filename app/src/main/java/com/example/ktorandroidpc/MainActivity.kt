package com.example.ktorandroidpc

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.example.ktorandroidpc.databinding.ActivityMainBinding
import com.example.ktorandroidpc.explorer.FileUtils
import com.example.ktorandroidpc.plugins.configureRouting
import com.example.ktorandroidpc.plugins.configureTemplating
import com.example.ktorandroidpc.utills.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var coroutineScope: CoroutineScope
    private var connectOrDisconnect = true
    private val mDirectory by lazy { Environment.getExternalStorageDirectory().absolutePath }
    private lateinit var mStoreRootFolder: List<FileModel>

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Const.PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED}
        ) {
            Executional()
        }
        else{
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
        Tools.requestForAllPermission(this@MainActivity)

        Initiallizers()

        ClickListener()

        if (Tools.checkAllPermission(this@MainActivity)) {
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
        mStoreRootFolder = StoreRootFolder()
        coroutineScope.launch {
            DataManager.with(this@MainActivity.application)
                .savePreferenceData(mStoreRootFolder, Const.ROOT_FOLDER_KEY)
        }
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
    }

    private fun StoreRootFolder(): List<FileModel> {
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                mDirectory,
                showHiddenFiles = true
            )
        )
    }
}
