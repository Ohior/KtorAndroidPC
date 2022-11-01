package com.example.ktorandroidpc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ktorandroidpc.adapter.DownloadAdapter
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.databinding.ActivityMainBinding
import com.example.ktorandroidpc.explorer.FileType
import com.example.ktorandroidpc.plugins.*
import com.example.ktorandroidpc.utills.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.util.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import java.io.File
import java.lang.reflect.Method
import java.net.URI


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var coroutineScope: CoroutineScope
    private var connectDevice = true
    private lateinit var recyclerViewCoroutineScope: CoroutineScope
    private var sdDirectory: String? = null
    private lateinit var recyclerAdapter: RecyclerAdapter

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Const.PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            Execution()
        } else {
            Tools.requestForAllPermission(this@MainActivity)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        if (coroutineScope.isActive || ::coroutineScope.isInitialized) {
            coroutineScope.cancel()
            recyclerViewCoroutineScope.cancel()
        }
        baseContext.cacheDir.deleteRecursively()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Tools.requestForAllPermission(this)

        Initializers()

        ClickListener()

        if (Tools.checkAllPermission(this)) {
            Execution()
        }

    }

    private fun Execution() {
        Tools.createDirectoryIfNonExist()
        DataManager.with(application).setString(Const.SD_DIRECTORY_KEY, sdDirectory)
//        Glide.with(applicationContext).load(R.drawable.gifimage).into(binding.idGitImage)
        if (!HotspotIsOn()) {
            binding.idRootView.displaySnackBar("Do you want to connect to Hotspot", "connect") {
                ConnectHotspot()
            }
        }

//        Tools.popUpWindow(this, "Download", R.layout.download_progress_bar) {layout, popup->
//            val progress = layout.findViewById<ProgressBar>(R.id.id_download_progressbar)
//            val title = layout.findViewById<TextView>(R.id.id_tv_title)
//            var count = progress.progress
//            Thread {
//                while (count < 100){
//                    count++
//                    Handler(Looper.getMainLooper()).post {
//                        progress.progress = count
//                        title.text = count.toString() + "/" + progress.max
//                    }
//                        Thread.sleep(100)
//                }
//            }.start()
//        }

        DownloadAdapterFunction()
    }

    private fun DownloadAdapterFunction() {
        val downloadAdapter = DownloadAdapter(this, binding.idRecyclerView)
    }

    private fun ClickListener() {
        binding.idBtnConnect2browser.setOnClickListener {
            if (HotspotIsOn()) {
                if (connectDevice) {
                    coroutineScope.launch {
                        embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
                            configureRouting()
                            configureTemplating(this)
                        }.start(wait = true)
                    }
                    binding.idBtnConnect2browser.text = getString(R.string.format_string, "Disconnect PC")
                    binding.idRootView.displaySnackBar("Connected Address is ${Const.ADDRESS}")
                    ProgressMonitor()
                } else {
                    binding.idBtnConnect2browser.text = getString(R.string.format_string, "Connect PC")
                    coroutineScope.cancel()
                    recyclerViewCoroutineScope.cancel()
                }
                connectDevice = !connectDevice
            } else {
                ConnectHotspot()
            }
        }

        binding.idBtnConnectWithApplication.setOnClickListener {
            val intent = Intent(this, ExplorerActivity::class.java)
            startActivity(intent)
            coroutineScope.cancel()
        }
    }

    private fun Initializers() {
        coroutineScope = CoroutineScope(Dispatchers.IO)
        recyclerAdapter = RecyclerAdapter(
            applicationContext,
            binding.idRecyclerView,
            R.layout.explorer_item2)
        sdDirectory = GetExternalSDCardRootDirectory()
    }

    private fun GetExternalSDCardRootDirectory(): String? {
        if (Tools.isExternalStorageAvailable() || Tools.isExternalStorageReadOnly()) {
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
        return null
    }

    private fun ConnectHotspot() {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val componentName = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.component = componentName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun HotspotIsOn(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method: Method = wifiManager.javaClass.getMethod("getWifiApState")
        method.isAccessible = true
        val invoke = method.invoke(wifiManager) as Int
        return invoke == 13
    }

    private fun ProgressMonitor() {
        recyclerViewCoroutineScope = CoroutineScope(Dispatchers.IO)
        recyclerViewCoroutineScope.launch {
            DataManager.with(application).clearSharedPreferenceKey(Const.PROGRESS_KEY)
            while (true) {
                val data = DataManager.getPreferenceData<ProgressDataClass>(Const.PROGRESS_KEY)
                if (data != null) {
                    launch(Dispatchers.Main){
                        binding.idGifLl.visibility = View.GONE
                        binding.idRecyclerView.visibility = View.VISIBLE
                        recyclerAdapter.addToAdapter(
                            RecyclerAdapterDataclass(
                                name = data.dataName,
                                detail = Const.OH_TRANSFER_PATH,
                                drawable = R.drawable.video,
                                fileType = FileType.FILE
                            )
                        )
                        Tools.debugMessage(data.toString(), "ProgressDataClass")
                        DataManager.with(application).clearSharedPreferenceKey(Const.PROGRESS_KEY)
                    }
                }
            }
        }
    }
}
