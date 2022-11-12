package com.example.ktorandroidpc.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ktorandroidpc.*
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.explorer.FileType
import com.example.ktorandroidpc.explorer.FileUtils
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
    lateinit var idGifLinearLayout: LinearLayout
    private lateinit var idGifImageView: ImageView
    private lateinit var idRecyclerView: RecyclerView
    private lateinit var idBtnConnectBrowser: Button
    private lateinit var coroutineScope: CoroutineScope
    private var connectDevice = true
    private var sdDirectory: String? = null
    lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var nettyEngine: NettyApplicationEngine
    private lateinit var idToolbarTextView: TextView
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var deleteFileUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        clear menu item so as not to duplicate items
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
                //  save root path in shared preference, so you can get and use  them from other fragment
                // TODO: 11/11/2022 optimize data communication between fragment
                DataManager.with(requireActivity()).putPreferenceData(
                    StorageDataClass(
                        rootDirectory = Const.ROOT_PATH,
                        isSdStorage = false
                    ), Const.FRAGMENT_DATA_KEY
                )
                Navigation.findNavController(fragmentView).navigate(R.id.connectPcFragment_to_explorerFragment)
                true
            }
            R.id.id_menu_sd -> {
                // save root path in shared preference, so you can get and use  them from other fragment
                // TODO: 11/11/2022 optimize data communication between fragment
                DataManager.with(requireActivity()).putPreferenceData(
                    StorageDataClass(
                        rootDirectory = GetExternalSDCardRootDirectory().toString(),
                        isSdStorage = true
                    ), Const.FRAGMENT_DATA_KEY
                )
                Navigation.findNavController(fragmentView).navigate(R.id.connectPcFragment_to_explorerFragment)
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

        requireActivity().findViewById<TextView>(R.id.id_tv_toolbar).isClickable = false

        Initializers()

        ClickListener()

        if (Tools.checkAllPermission(requireActivity())) {
            FragmentExecutable()
        }

        return fragmentView
    }

    private fun FragmentExecutable() {
        Tools.createDirectoryIfNonExist(Const.UPLOAD_PATH)
        Glide.with(requireActivity()).asGif().load(R.drawable.gifimage).into(idGifImageView)
        if (!IsHotspotOn()) {
            fragmentView.displaySnackBar("Wifi - Hotspot is switch OFF!", "switch ON") {
                ConnectHotspot()
            }
        }
        DownloadAdapterFunction()
    }

    private fun DownloadAdapterFunction() {
//        display uploaded items in recyclerview
        recyclerAdapter.onClickListener(object : RecyclerAdapter.OnItemClickListener {
            override fun onMenuClick(fileModel: FileModel, view: View) {
                // when recycler view menu is clicked, display drop down menu
                requireContext().popupMenu(view) { menuItem ->
                    when (menuItem.itemId) {
                        R.id.id_rv_menu_delete -> {

                            requireContext().popUpWindow("This delete is permanent", "Delete") { adb ->
                                adb.setPositiveButton("delete") { _, _ ->
                                    // when delete menu is clicked, display popup to confirm delete
                                    Tools.deleteFileFromStorage(
                                        fileModel.file,
                                        requireContext()
                                    ) { intentSender ->
                                        intentSender.let { sender ->
                                            intentSenderLauncher.launch(
                                                IntentSenderRequest.Builder(sender).build()
                                            )
                                        }
                                    }
                                    recyclerAdapter.arrayList.remove(RecyclerAdapterDataclass(fileModel))
                                    recyclerAdapter.notifyDataSetChanged()
                                }
                                adb.setNegativeButton("cancel") { _, _ ->
                                    adb.show().dismiss()
                                }
                            }
                        }
                        R.id.id_rv_menu_open -> {
                            // open menu is clicked
                            if (requireContext().openFileWithDefaultApp(fileModel.file)) {
                                Tools.showToast(requireContext(), "No App To open this File! 😢")
                            }
                        }
                    }
                }
            }
        })

    }

    private fun ClickListener() {
//        check if connect button is clicked, so you can connect or disconnect users
        idBtnConnectBrowser.setOnClickListener {
            if (IsHotspotOn()) {
                // check if device can be connected
                if (connectDevice) {
                    coroutineScope.launch {
                        // launch connection
                        nettyEngine = embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
                            configureRouting { it1 ->
                                it1.uploadFile {
                                    displayRecyclerView(it)
                                }
                            }

                            configureTemplating(this)
                        }
                        // start connection
                        nettyEngine.start(wait = true)
                    }
                    idBtnConnectBrowser.text = getString(R.string.format_string, "Disconnect PC")
                    fragmentView.displaySnackBar("Connected to Address ${Const.ADDRESS}")
                } else {
                    // stop connection because connectDevice is false
                    nettyEngine.stop()
                    idBtnConnectBrowser.text = getString(R.string.format_string, "Connect PC")
                }
                // set connectDevice to not connectDevice so that you can toggle connection
                connectDevice = !connectDevice
            } else {
                // launch intent to prompt user to switch on hotspot
                ConnectHotspot()
            }
        }
    }

    private fun Initializers() {
//        initialize all global variables
        idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
        idToolbarTextView.text = requireActivity().getString(R.string.app_name)
        idGifImageView = fragmentView.findViewById(R.id.id_gif_image)
        idGifLinearLayout = fragmentView.findViewById(R.id.id_gif_ll)
        idRecyclerView = fragmentView.findViewById(R.id.id_recycler_view)
        idBtnConnectBrowser = fragmentView.findViewById(R.id.id_btn_connect_browser)
        coroutineScope = CoroutineScope(Dispatchers.IO)
        idToolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_icon, 0, 0, 0)
        recyclerAdapter = RecyclerAdapter(
            requireContext(),
            idRecyclerView,
            R.layout.explorer_item
        )
        sdDirectory = GetExternalSDCardRootDirectory()
    }

    fun GetExternalSDCardRootDirectory(): String? {
//        get the root directory of sd card if there is any, return null otherwise
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
//        open intent window so user can on their moble hotsopt
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val componentName = ComponentName("com.android.settings", "com.android.settings.TetherSettings")
        intent.component = componentName
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun IsHotspotOn(): Boolean {
        val wifiManager = requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val method: Method = wifiManager.javaClass.getMethod("getWifiApState")
        method.isAccessible = true
        val invoke = method.invoke(wifiManager) as Int
        return invoke == 13
    }

    private fun displayRecyclerView(file: File) {
//        display updated item and update recyclerview with items
        CoroutineScope(Dispatchers.Main).launch {
            if (idGifLinearLayout.isVisible) {
                idRecyclerView.visibility = RecyclerView.VISIBLE
                idGifLinearLayout.visibility = LinearLayout.GONE
            }
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = FileModel(
                        name = file.name,
                        fileType = FileType.getFileType(file),
                        path = file.path,
                        sizeInMB = FileUtils.convertFileSizeToMB(file.length())
                    ),
                )
            )
            recyclerAdapter.notifyDataSetChanged()
        }
    }

    fun registerDeleteResult() {
        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && deleteFileUri != null) {
                    deleteFileUri =
                        Tools.deleteFileFromStorage(deleteFileUri!!.toFile(), requireContext()) { intentSender ->
                            intentSender.let { sender ->
                                intentSenderLauncher.launch(
                                    IntentSenderRequest.Builder(sender).build()
                                )
                            }
                        }
                }
                Tools.showToast(requireContext(), "File deleted Successfully")
            } else {
                Tools.showToast(requireContext(), "File delete Aborted")
            }
        }
    }


}

