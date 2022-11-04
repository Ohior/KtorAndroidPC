package com.example.ktorandroidpc.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.displaySnackBar
import com.example.ktorandroidpc.plugins.*
import com.example.ktorandroidpc.utills.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.reflect.Method

class ConnectPcFragment : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var idGifLinearLayout: LinearLayout
    private lateinit var idGifImageView: ImageView
    private lateinit var idRecyclerView: RecyclerView
    private lateinit var idBtnConnectBrowser: Button
    private lateinit var coroutineScope: CoroutineScope
    private var connectDevice = true
    private var sdDirectory: String? = null
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var nettyEngine: NettyApplicationEngine
    private lateinit var idToolbarTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
        idToolbarTextView.text = requireActivity().getString(R.string.app_name)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.id_menu_mobile)?.isVisible = true
        if (sdDirectory == null) {
            menu.findItem(R.id.id_menu_sd)?.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.id_menu_mobile -> {
                Navigation.findNavController(fragmentView).navigate(R.id.connectPcFragment_to_explorerFragment)
                true
            }
            R.id.id_menu_sd -> {
                Navigation.findNavController(fragmentView).navigate(R.id.connectPcFragment_to_sdExplorerFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onDestroyView() {
        try {
            nettyEngine.stop()
        } catch (e: UninitializedPropertyAccessException) {
            super.onDestroyView()
        }
        super.onDestroyView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_connect_pc, container, false)

        Tools.requestForAllPermission(requireActivity())

        Initializers()

        ClickListener()

        if (Tools.checkAllPermission(requireActivity())) {
            FragmentExecutable()
        }

        return fragmentView
    }

    private fun FragmentExecutable() {
        Tools.createDirectoryIfNonExist()
        DataManager.with(requireActivity()).setString(Const.SD_DIRECTORY_KEY, sdDirectory)
        Glide.with(requireActivity()).asGif().load(R.drawable.gifimage).into(idGifImageView)
        if (!HotspotIsOn()) {
            fragmentView.displaySnackBar("Wifi - Hotspot is switch OFF!", "switch ON") {
                ConnectHotspot()
            }
        }
        DownloadAdapterFunction()
    }

    private fun DownloadAdapterFunction() {
        recyclerAdapter.onClickListener(object : RecyclerAdapter.OnItemClickListener {

        })

    }

    private fun ClickListener() {
        idBtnConnectBrowser.setOnClickListener {
            if (HotspotIsOn()) {
                if (connectDevice) {
                    coroutineScope.launch {
                        nettyEngine = embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
//                            configureRouting()

                            configureRouting {
                                it.uploadFile()
                                it.downloadFile()
                            }

                            configureTemplating(this)
                        }
                        nettyEngine.start(wait = true)
                    }
                    idBtnConnectBrowser.text = getString(R.string.format_string, "Disconnect PC")
                    fragmentView.displaySnackBar("Connected to Address ${Const.ADDRESS}")
                } else {
                    nettyEngine.stop()
                    idBtnConnectBrowser.text = getString(R.string.format_string, "Connect PC")
                }
                connectDevice = !connectDevice
            } else {
                ConnectHotspot()
            }
        }
    }

    private fun Initializers() {
        idGifImageView = fragmentView.findViewById(R.id.id_gif_image)
        idGifLinearLayout = fragmentView.findViewById(R.id.id_gif_ll)
        idRecyclerView = fragmentView.findViewById(R.id.id_recycler_view)
        idBtnConnectBrowser = fragmentView.findViewById(R.id.id_btn_connect_browser)
        coroutineScope = CoroutineScope(Dispatchers.IO)
        recyclerAdapter = RecyclerAdapter(
            requireContext(),
            idRecyclerView,
            R.layout.explorer_item
        )
        sdDirectory = GetExternalSDCardRootDirectory()
    }

    private fun GetExternalSDCardRootDirectory(): String? {
        if (Tools.isExternalStorageAvailable() || Tools.isExternalStorageReadOnly()) {
            val storageManager = requireActivity().getSystemService(Context.STORAGE_SERVICE)
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

    private fun ConnectHotspot() {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val componentName = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.component = componentName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun HotspotIsOn(): Boolean {
        val wifiManager = requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method: Method = wifiManager.javaClass.getMethod("getWifiApState")
        method.isAccessible = true
        val invoke = method.invoke(wifiManager) as Int
        return invoke == 13
    }

    fun displayRecyclerView(totalBytes: Long) {
        val size = String.format("%dm", totalBytes / 1024 / 1024)
    }

}