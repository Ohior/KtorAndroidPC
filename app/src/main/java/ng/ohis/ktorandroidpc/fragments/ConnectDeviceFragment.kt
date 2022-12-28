package ng.ohis.ktorandroidpc.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.*
import ng.ohis.ktorandroidpc.classes.ExplorerInterface
import ng.ohis.ktorandroidpc.classes.MyBroadcastReceiver
import ng.ohis.ktorandroidpc.locationPopUpWindow
import ng.ohis.ktorandroidpc.popUpWindow
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools


class ConnectDeviceFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idNavigateRecyclerView: RecyclerView
    private lateinit var navbarRecyclerAdapter: NavbarRecyclerAdapter
    private lateinit var idBtnConnectDevice: Button
    private lateinit var idTvLocalStorage: TextView
    private lateinit var idTvSdStorage: TextView

    private lateinit var mManager: WifiP2pManager
    private lateinit var wifiManager: WifiManager
    private lateinit var mChannel: WifiP2pManager.Channel

    private var mReceiver: BroadcastReceiver? = null
    private lateinit var mIntentFilter: IntentFilter

    private var peers = ArrayList<WifiP2pDevice>()
    private lateinit var deviceNameArray: ArrayList<String>
    private lateinit var deviceArray: ArrayList<WifiP2pDevice>

//    lateinit var clientClass: ClientClass
//    lateinit var serverClass: ServerClass
//    lateinit var sendReceive: SendReceive

    private lateinit var rootDir: StorageDataClass
    private var filePath = ""


    // REQUEST PERMISSION
    private val requestPermission =
        registerForActivityResult(
            RequestPermission()
        ) {}

    // WIFI Receiver Contract
    private val getWifiResult =
        registerForActivityResult(
            StartActivityForResult()
        ) {
            if (wifiManager.isWifiEnabled) {
                Tools.showToast(requireContext(), "Permission was granted, successfully")
            } else {
                requireActivity().popUpWindow(
                    title = "enable wifi",
                    message = "Wifi is needed for ${Const.APP_NAME} to work"
                ) { it.setCancelable(true) }
            }
        }

    // LOCATION Launcher
    private val getLocationResult =
        registerForActivityResult(
            StartActivityForResult()
        ) {}

    // Listen For Peers
    val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        if (peerList.deviceList != peers) {
            peers.clear()
            peers.addAll(peerList.deviceList)
            deviceNameArray = ArrayList()
            deviceArray = ArrayList()

            for (device in peerList.deviceList) {
                deviceNameArray.add(device.deviceName)
                deviceArray.add(device)
                Tools.debugMessage(device.deviceName)
            }
        }
        if (peers.isEmpty()) {
            Tools.showToast(requireContext(), "Peers is Empty")
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().registerReceiver(mReceiver, mIntentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mReceiver)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_connect_device, container, false)

        fragmentInitializers()

        fragmentExecutables()

        return fragmentView
    }


    override fun navigateDirectoryForward(
        position: Int?,
        recyclerAdapter: RecyclerAdapter,
        context: Context,
        filePath: String
    ): String {
        this.filePath = super.navigateDirectoryForward(position, recyclerAdapter, context, filePath)
        navbarRecyclerView()
        return this.filePath
    }

    override fun navigateDirectoryBackward(
        recyclerAdapter: RecyclerAdapter,
        rootDir: String,
        filePath: String
    ): String {
        this.filePath = super.navigateDirectoryBackward(recyclerAdapter, rootDir, filePath)
        navbarRecyclerView()
        return this.filePath
    }

    private fun fragmentInitializers() {
        rootDir = Gson().fromJson(
            requireArguments().getString(Const.FRAGMENT_DATA_KEY),
            StorageDataClass::class.java
        )
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
        idTvLocalStorage = fragmentView.findViewById(R.id.id_tv_local_storage)
        idTvSdStorage = fragmentView.findViewById(R.id.id_tv_sd_storage)
        recyclerAdapter = RecyclerAdapter(
            requireContext(),
            idRvRootFolder,
            R.layout.explorer_item,
            menuVisibility = false
        )
        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
        idBtnConnectDevice = fragmentView.findViewById(R.id.id_btn_connect_device)
        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)
        filePath = rootDir.rootDirectory
        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)

        wifiManager =
            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mManager = requireActivity().applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(requireContext(), Looper.getMainLooper(), null)

        deviceNameArray = ArrayList()

        mReceiver = MyBroadcastReceiver(mManager = mManager, mChannel = mChannel, mActivity = this)

        mIntentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    private fun fragmentExecutables() {
        // this makes sure pressing the back button only exit
        // this fragment when user is at root directory
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
        requireActivity().findViewById<TextView>(R.id.id_tv_toolbar).apply {
            text = getToolbarName(null, requireActivity(), "Connect Device")
            setOnClickListener {
                filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
                if (filePath.isEmpty()) {
                    requireActivity().onBackPressed()
                }
            }
        }

        rootDirectoryNavigation()

        recyclerViewClickListener()

        inflateMenuItem()

        connectDeviceButton()
    }

    private fun rootDirectoryNavigation() {
        idTvLocalStorage.setOnClickListener {
            rootDir = StorageDataClass(
                rootDirectory = Const.ROOT_PATH,
                isSdStorage = false
            )
            filePath = navigateDirectoryForward(
                null,
                recyclerAdapter,
                requireContext(),
                rootDir.rootDirectory
            )
        }

        idTvSdStorage.setOnClickListener {
            rootDir = StorageDataClass(
                rootDirectory = Tools.getExternalSDCardRootDirectory(requireActivity())!!,
                isSdStorage = true
            )
            filePath = navigateDirectoryForward(
                null,
                recyclerAdapter,
                requireContext(),
                rootDir.rootDirectory
            )
        }
    }

    private fun connectDeviceButton() {
        idBtnConnectDevice.setOnClickListener {
            when {
                Tools.isPermissionGranted(requireContext(), Const.FINE_LOCATION_PERMISSION) -> {
                    if (!Tools.isLocationEnabled(requireContext())) {
                        getLocationResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    if (!wifiManager.isWifiEnabled) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Caller
                            val intent = Intent(Settings.Panel.ACTION_WIFI)
                            getWifiResult.launch(intent)
                        } else {
                            wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
                        }
                    } else {
                        deviceConnectionPopup()
                        idBtnConnectDevice.visibility = View.GONE
                    }
                }
                shouldShowRequestPermissionRationale(Const.FINE_LOCATION_PERMISSION) -> {
                    requireActivity().popUpWindow(
                        title = "Permission",
                        message = "Permissions 🙉 are needed for this app 📳 to run successfully"
                    ) {
                        it.setCancelable(true)
                        it.setPositiveButton("Grant permission") { _, _ ->
                            requestPermission.launch(Const.FINE_LOCATION_PERMISSION)
                        }
                    }
                }
                else -> {
                    requestPermission.launch(Const.FINE_LOCATION_PERMISSION)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun deviceConnectionPopup() {
        requireActivity().locationPopUpWindow(
            fragmentView = fragmentView,
            R.layout.device_connection_popup,
        ) { v, p ->
            p.setOnDismissListener {
                idBtnConnectDevice.visibility = View.VISIBLE
            }
            val itcs = v.findViewById<TextView>(R.id.id_tv_connect_status)
            mManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    itcs.setBackgroundColor(Color.LTGRAY)
                    itcs.text =
                        requireActivity().getString(R.string.format_string, "Discovery Started")
                }

                override fun onFailure(p0: Int) {
                    itcs.setBackgroundColor(Color.DKGRAY)
                    itcs.text =
                        requireActivity().getString(R.string.format_string, "Discovery Failed")
                }
            })
            val ild = v.findViewById<ListView>(R.id.id_lv_devices)
            v.findViewById<Button>(R.id.id_btn_discover_devices).setOnClickListener {
                ild.adapter =
                    ArrayAdapter(requireContext(), R.layout.list_item, R.id.id_tv_list_item, deviceNameArray)
            }
            ild.setOnItemClickListener { _, _, position, _ ->
                val device = deviceArray[position]
                val config = WifiP2pConfig()
                config.deviceAddress = device.deviceAddress
                mManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Tools.showToast(requireContext(), "Connected to ${device.deviceName}")
                    }

                    override fun onFailure(p0: Int) {
                        Tools.showToast(
                            requireContext(),
                            "Could not connected to ${device.deviceName}"
                        )
                    }
                })
            }
        }
    }

    private fun recyclerViewClickListener() {
        recyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath =
                    navigateDirectoryForward(position, recyclerAdapter, requireContext(), filePath)
            }
        })
    }

    private fun navbarRecyclerView() {
        navbarRecyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath = filePath.split("/")
                    .dropLastWhile { it != navbarRecyclerAdapter.getItemAt(position).name }
                    .joinToString("/")
                filePath =
                    navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
            }
        })
        updateNavigationBarFolderRecyclerView(filePath, rootDir, navbarRecyclerAdapter)
    }

    private fun inflateMenuItem() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.main_menu, menu)
                menu.findItem(R.id.id_menu_connect_device).isVisible = false
                menu.findItem(R.id.id_menu_mobile).isVisible = false
                menu.findItem(R.id.id_menu_sd).isVisible = false
                menu.findItem(R.id.id_menu_computer).isVisible = true

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.id_menu_computer -> {
                        Tools.navigateFragmentToFragment(
                            this@ConnectDeviceFragment,
                            R.id.connectPcFragment
                        )
                        true
                    }
                    else -> false
                }
            }

        })
    }
}
