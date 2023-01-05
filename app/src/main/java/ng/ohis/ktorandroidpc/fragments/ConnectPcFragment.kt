package ng.ohis.ktorandroidpc.fragments

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ng.ohis.ktorandroidpc.*
import ng.ohis.ktorandroidpc.adapter.*
import ng.ohis.ktorandroidpc.classes.NavbarMenuInterface
import ng.ohis.ktorandroidpc.classes.NavbarMenuInterfaceImp
import ng.ohis.ktorandroidpc.plugins.configureRouting
import ng.ohis.ktorandroidpc.plugins.configureTemplating
import ng.ohis.ktorandroidpc.plugins.uploadFile
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.File


class ConnectPcFragment : Fragment(), NavbarMenuInterface by NavbarMenuInterfaceImp() {
    private lateinit var fragmentView: View
    private lateinit var idGifLinearLayout: LinearLayout
    private lateinit var idGifImageView: ImageView
    private lateinit var idRecyclerView: RecyclerView
    private lateinit var idBtnConnectBrowser: Button

    //    private lateinit var idBtnConnectDevice: Button
    private lateinit var coroutineScope: CoroutineScope
    private var connectDevice = true
    private lateinit var sdDirectory: StorageDataClass
    private lateinit var recyclerAdapter: RecyclerAdapter
    private var nettyEngine: NettyApplicationEngine? = null
    private lateinit var idToolbarTextView: TextView
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var deleteFileUri: Uri? = null


    override fun onDestroyView() {
        try {
            nettyEngine?.stop()
            nettyEngine = null
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

        CoroutineScope(Dispatchers.IO).launch {
            Tools.requestForAllPermission(requireActivity())
        }

        requireActivity().findViewById<TextView>(R.id.id_tv_toolbar).isClickable = false

        fragmentInitializers()

        inflateMenuItem()

        buttonClickListener()

        if (Tools.checkAllPermission(requireActivity())) {
            fragmentExecutable()
        }

        return fragmentView
    }

    private fun fragmentExecutable() {
        // Create app (Chransver) folder in root directory if it does not exist
        Tools.createDirectoryIfNonExist(Const.ROOT_PATH)
        Glide.with(requireActivity()).asGif().load(R.drawable.gifimage).into(idGifImageView)
        if (!Tools.isHotspotOn(requireActivity())) {
            fragmentView.displaySnackBar("Wifi - Hotspot is switch OFF!", "switch ON") {
                Tools.connectHotspot(requireActivity())
            }
        }
        receivedRecyclerAdapter()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun receivedRecyclerAdapter() {
//        display uploaded items in recyclerview
        recyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onMenuClick(fileModel: FileModel, view: View, position: Int) {
                // when recycler view menu is clicked, display drop down menu
                PopupMenu(context, view).apply {
                    this.inflate(R.menu.rv_menu_item)
                    this.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.id_rv_menu_delete -> {
                                requireContext().popUpWindow(
                                    "This action is permanent",
                                    "Delete File"
                                ) { builder ->
                                    builder.setPositiveButton("Delete") { _, _ ->
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
                                        // update adapter
                                        recyclerAdapter.removeAt(RecyclerAdapterDataclass(fileModel))
                                        recyclerAdapter.notifyItemRemoved(position)
                                    }
                                    builder.setNegativeButton("Cancel") { _, _ ->
                                        builder.show().dismiss()
                                    }
                                }
                                true
                            }
                            R.id.id_rv_menu_open -> {
                                if (!requireContext().openFileWithDefaultApp(fileModel.file)) {
                                    Tools.showToast(
                                        requireContext(),
                                        "No App To open this File! 😢"
                                    )
                                }
                                true
                            }
                            R.id.id_rv_menu_detail -> {
                                requireContext().popUpWindow(
                                    title = "Properties : ",
                                    message = MenuDetailDataClass(fileModel).toString()
                                ) { it.setCancelable(true) }
                                true
                            }
                            else -> false
                        }
                    }
                    this.show()
                }
            }
        })

    }

    private fun buttonClickListener() {
//        check if connect button is clicked, so you can connect or disconnect users
        idBtnConnectBrowser.setOnClickListener {
            if (Tools.isHotspotOn(requireActivity())) {
//                idBtnConnectDevice.isEnabled = !connectDevice
                // check if device can be connected
                if (connectDevice) {
                    coroutineScope.launch {
                        // launch connection
                        nettyEngine =
                            embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
                                configureRouting { it1 ->
                                    it1.uploadFile {
                                        displayRecyclerView(it)
                                    }
                                }
                                configureTemplating(this)
                            }
                        // start connection
                        nettyEngine?.start(wait = true)
                    }
                    idBtnConnectBrowser.text = getString(R.string.format_string, "Disconnect PC")
                    fragmentView.displaySnackBar("Connected to Address ${Const.ADDRESS}")
                    requireActivity().toggleScreenWakeLock(true)
                } else {
                    // stop connection because connectDevice is false
                    nettyEngine?.stop()
                    nettyEngine = null
                    idBtnConnectBrowser.text = getString(R.string.format_string, "Connect PC")
                    requireActivity().toggleScreenWakeLock(false)
                }
                // set connectDevice to not connectDevice so that you can toggle connection
                connectDevice = !connectDevice
            } else {
                // launch intent to prompt user to switch on hotspot
                Tools.connectHotspot(requireActivity())
            }
        }
    }

    private fun fragmentInitializers() {
//        initialize all global variables
        idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
        idToolbarTextView.text = requireActivity().getString(R.string.app_name)
        idGifImageView = fragmentView.findViewById(R.id.id_gif_image)
        idGifLinearLayout = fragmentView.findViewById(R.id.id_gif_ll)
        idRecyclerView = fragmentView.findViewById(R.id.id_recycler_view)
        idBtnConnectBrowser = fragmentView.findViewById(R.id.id_btn_connect_browser)
//        idBtnConnectDevice = fragmentView.findViewById(R.id.id_btn_connect_device)
        coroutineScope = CoroutineScope(Dispatchers.IO)
        idToolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.ic_icon,
            0,
            0,
            0
        )
        recyclerAdapter = RecyclerAdapter(
            requireContext(),
            idRecyclerView,
            R.layout.explorer_item
        )
        sdDirectory = StorageDataClass(
            isSdStorage = Tools.getExternalSDCardRootDirectory(requireActivity()) != null,
            rootDirectory = ""
        )
    }

    private fun displayRecyclerView(file: File) {
        /*
        When there is an upload of files this function executes and the gif image is gone
        to give way for the recyclerview adapter to display
         */
        CoroutineScope(Dispatchers.Main).launch {
            if (idGifLinearLayout.isVisible) {
                //  display recyclerview with items
                // and remove gif layout
                idRecyclerView.visibility = RecyclerView.VISIBLE
                idGifLinearLayout.visibility = LinearLayout.GONE
            }
            //add stuff to recyclerview
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = FileModel(
                        file = file
                    ),
                )
            )
            recyclerAdapter.notifyItemInserted(recyclerAdapter.itemCount)
        }
    }

    private fun menuItemClicked(function: () -> Unit) {
        if (nettyEngine != null) {
            requireContext().popUpWindow(
                title = "Notice 🔔",
                message = "PC Connection is in progress. Leaving this page 📟 will result in connection lost, which may lead to interruption of your download 👇🏾 or upload 👆🏾."
            ) { popup ->
                popup.setCancelable(true)
                popup.setPositiveButton("Continue") { _, _ ->
                    function()
                }
                popup.setNegativeButton("Cancel") { _, _ ->
                    popup.show().dismiss()
                }
            }
        } else function()
    }

    fun registerDeleteResult() {
        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && deleteFileUri != null) {
                        deleteFileUri =
                            Tools.deleteFileFromStorage(
                                deleteFileUri!!.toFile(),
                                requireContext()
                            ) { intentSender ->
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

    private fun inflateMenuItem() {
        navbarMenuProvider(
            requireActivity(),
            sdDirectory,
            ShowSdCardDataclass(Tools.isExternalStorageAvailable(requireContext()), localMemory = true, connectPc = false)
        ) {
            when (it.itemId) {
                R.id.id_menu_mobile -> {
                    menuItemClicked {
                        Tools.navigateToFragment(
                            fragment = this@ConnectPcFragment,
                            fragId = R.id.explorerFragment,
                            storageDataJson = StorageDataClass(
                                rootDirectory = Const.ROOT_PATH,
                                isSdStorage = false
                            ).toJson()
                        )
                    }
                    true
                }
                R.id.id_menu_sd -> {
                    menuItemClicked {
                        Tools.navigateToFragment(
                            fragment = this@ConnectPcFragment,
                            fragId = R.id.explorerFragment,
                            storageDataJson = StorageDataClass(
                                rootDirectory = Tools.getExternalSDCardRootDirectory(
                                    requireActivity()
                                ).toString(),
                                isSdStorage = true
                            ).toJson()
                        )
                    }
                    true
                }
                R.id.id_menu_connect_device -> {
                    menuItemClicked {
                        Tools.navigateToFragment(
                            fragment = this@ConnectPcFragment,
                            fragId = R.id.connectDeviceFragment,
                            storageDataJson = StorageDataClass(
                                rootDirectory = Const.ROOT_PATH,
                                isSdStorage = false
                            ).toJson()
                        )
                    }

                    true
                }
                else -> false
            }
        }
    }
}

