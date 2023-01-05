package ng.ohis.ktorandroidpc.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.InetAddresses
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.*
import ng.ohis.ktorandroidpc.classes.*
import ng.ohis.ktorandroidpc.locationPopUpWindow
import ng.ohis.ktorandroidpc.popUpWindow
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools
import java.net.InetAddress
import java.util.concurrent.Executors


class ConnectDeviceFragment : Fragment(), ExplorerInterface,
    NavbarMenuInterface by NavbarMenuInterfaceImp() {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idNavigateRecyclerView: RecyclerView
    private lateinit var navbarRecyclerAdapter: NavbarRecyclerAdapter
    private lateinit var idBtnConnectDevice: Button
    private lateinit var idTvLocalStorage: TextView
    private lateinit var idTvSdStorage: TextView

    private val manager: WifiP2pManager? by lazy(LazyThreadSafetyMode.NONE) {
        requireActivity().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    var channel: WifiP2pManager.Channel? = null
    var receiver: MyBroadcastReceiver? = null

    private var inetAddress: InetAddress? = null

    private lateinit var wifiManager: WifiManager
    private lateinit var intentFilter: IntentFilter

    private var peers = ArrayList<WifiP2pDevice>()

    private lateinit var deviceNameArray: ArrayList<String>
    private lateinit var deviceArray: ArrayList<WifiP2pDevice>

    val peerListListener = WifiP2pManager.PeerListListener {
        if (!it.equals(peers)) {
            peers.clear()
            peers.addAll(it.deviceList)
            deviceNameArray = ArrayList()
            deviceArray = ArrayList()
            for (device in it.deviceList) {
                deviceNameArray.add(device.deviceName)
                deviceArray.add(device)
            }
        }
    }

    //    private lateinit var manager: WifiP2pManager
//    private lateinit var wifiManager: WifiManager
//    private lateinit var channel: WifiP2pManager.Channel
//
//    private var receiver: BroadcastReceiver? = null
//    private lateinit var intentFilter: IntentFilter
//
//    private var peers = ArrayList<WifiP2pDevice>()
//    private lateinit var deviceNameArray: ArrayList<String>
//    private lateinit var deviceArray: ArrayList<WifiP2pDevice>
//
    lateinit var clientClass: ClientClass
    lateinit var serverClass: ServerClass

    //    lateinit var sendReceive: SendReceiveThread
//
    private lateinit var rootDir: StorageDataClass
    private var filePath = ""

    //
    private var isHost: Boolean? = null

    private val getLocationResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}

    private val getWifiResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
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

    val connectionInfoListener = WifiP2pManager.ConnectionInfoListener {
        inetAddress = it.groupOwnerAddress
        val groupOwnerAddress = it.groupOwnerAddress
        if (it.groupFormed && it.isGroupOwner) {
            isHost = true
            serverClass = ServerClass()
            serverClass.start()
            Tools.debugMessage("HOST NAME IS" + it.groupOwnerAddress)
        } else if (it.groupFormed) {
            isHost = false
            clientClass = ClientClass(groupOwnerAddress)
            clientClass.start()
        }
    }

    override fun onResume() {
        super.onResume()
        receiver?.also { receiver ->
            requireActivity().registerReceiver(receiver, intentFilter)
        }
    }

    override fun onPause() {
        super.onPause()
        receiver?.also { receiver ->
            requireActivity().unregisterReceiver(receiver)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentView = inflater.inflate(R.layout.fragment_connect_device, container, false)

        fragmentInitializers()

        fragmentExecutables()

        return fragmentView
    }

    private fun fragmentInitializers() {

        idBtnConnectDevice = fragmentView.findViewById(R.id.id_btn_connect_device)
        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)

        recyclerAdapter = RecyclerAdapter(requireContext(), idRvRootFolder, R.layout.explorer_item)
        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)

        rootDir = Gson().fromJson(
            requireArguments().getString(Const.FRAGMENT_DATA_KEY),
            StorageDataClass::class.java
        )
        filePath =
            navigateDirForward(null, recyclerAdapter, requireContext(), rootDir.rootDirectory)

        channel = manager?.initialize(this.requireContext(), Looper.getMainLooper(), null)
        channel?.also { channel ->
            receiver = MyBroadcastReceiver(manager!!, channel, this)
        }
        wifiManager =
            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        deviceNameArray = ArrayList()
        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    private fun fragmentExecutables() {
        connectDeviceClicked()

        navigateDirForward(null, recyclerAdapter, requireContext(), filePath)

        recyclerViewClickListener()
    }


    private fun connectDeviceClicked() {
        idBtnConnectDevice.setOnClickListener {
            when {
                Tools.isPermissionGranted(requireContext(), Const.FINE_LOCATION_PERMISSION) -> {
                    if (!Tools.isLocationEnabled(requireContext())) {
                        getLocationResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    } else if (Tools.isHotspotOn(requireActivity())) {
                        requireActivity().popUpWindow(
                            title = "Notice",
                            message = "Your wifi - hotspot is on, do disable it"
                        ) {
                            it.setPositiveButton("disable hotspot") { _, _ ->
                                Tools.connectHotspot(requireActivity())
                            }
                            it.setNegativeButton("cancel") { s, _ -> s.dismiss() }
                        }
                    } else if (!wifiManager.isWifiEnabled) {
                        getWifiResult.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                    } else {
                        connectDevicePopup()
                    }
                }
                shouldShowRequestPermissionRationale(Const.FINE_LOCATION_PERMISSION) -> {
                    requireActivity().popUpWindow(
                        title = "Permission",
                        message = "Permissions 🙉 are needed for this app 📳 to run successfully"
                    ) {
                        it.setCancelable(true)
                        it.setPositiveButton("Grant permission") { _, _ ->
//                            requestPermission.launch(Const.FINE_LOCATION_PERMISSION)
                            Tools.askForPermission(
                                requireActivity(),
                                Const.FINE_LOCATION_PERMISSION
                            )
                        }
                    }
                }
                else -> {
                    Tools.askForPermission(requireActivity(), Const.FINE_LOCATION_PERMISSION)
                }
            }
        }
    }

    private fun recyclerViewClickListener() {
        recyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath = navigateDirForward(
                    position,
                    recyclerAdapter,
                    requireContext(),
                    filePath
                ) {
                    Tools.debugMessage("shearing file")
                    //FileServerAsyncTask(this@ConnectDeviceFragment, it, inetAddress!!).start()
//                    val executor = Executors.newSingleThreadExecutor()
//                    executor.execute {
                    if (isHost == true) {
                        serverClass.writeFile(it.file) { size, count, name ->
                        }
                    } else if (isHost == false) {
                        clientClass.writeFile(it.file) { size, count, name ->
                        }
                    }
//                    }
                }
            }
        })

    }

    @SuppressLint("MissingPermission")
    private fun connectDevicePopup() {
        requireActivity().locationPopUpWindow(
            fragmentView = fragmentView,
            R.layout.device_connection_popup
        ) { v, p ->
            val btnDiscover = v.findViewById<Button>(R.id.id_btn_discover_devices)
            val listView = v.findViewById<ListView>(R.id.id_lv_devices)
            val connectDevice = v.findViewById<TextView>(R.id.id_tv_connect_status)

            if (Tools.isPermissionGranted(requireContext(), Const.FINE_LOCATION_PERMISSION)) {
                manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        connectDevice.text = requireActivity().getString(
                            R.string.format_string,
                            "Discovery started"
                        )
                    }

                    override fun onFailure(p0: Int) {
                        connectDevice.text = requireActivity().getString(
                            R.string.format_string,
                            "Discovery Failed"
                        )
                    }

                })
            }

            btnDiscover.setOnClickListener {
                listView.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.list_item,
                    R.id.id_tv_list_item,
                    deviceNameArray
                )
            }
            listView.setOnItemClickListener { parent, view, position, id ->
                val device = deviceArray[position]
                val config = WifiP2pConfig()
                config.deviceAddress = device.deviceAddress
                channel.also { channel ->
                    manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            idBtnConnectDevice.text = requireActivity().getString(
                                R.string.format_string,
                                "Connected to ${device.deviceName}"
                            )
                            idBtnConnectDevice.isClickable = false
                            p.dismiss()
                        }

                        override fun onFailure(p0: Int) {
                        }

                    })
                }
            }
        }
    }

    private fun navigateDirForward(
        position: Int?,
        recyclerAdapter: RecyclerAdapter,
        context: Context,
        filePath: String,
        function: ((fileModel: FileModel) -> Unit)? = null
    ): String {
        this.filePath =
            super.navigateDirectoryForward(position, recyclerAdapter, context, filePath) {
                if (function != null) {
                    function(it)
                }
            }
        navbarRecyclerView(
            navbarRecyclerAdapter, this.filePath, rootDir, this.recyclerAdapter, context
        )
        return this.filePath
    }


}

//    private val getLocationResult =
//        registerForActivityResult(
//            StartActivityForResult()
//        ) {}
//
//    private val getWifiResult =
//        registerForActivityResult(
//            StartActivityForResult()
//        ) {
//            if (wifiManager.isWifiEnabled) {
//                Tools.showToast(requireContext(), "Permission was granted, successfully")
//            } else {
//                requireActivity().popUpWindow(
//                    title = "enable wifi",
//                    message = "Wifi is needed for ${Const.APP_NAME} to work"
//                ) { it.setCancelable(true) }
//            }
//        }
//
//    val peerListListener = WifiP2pManager.PeerListListener {
//        if (!it.equals(peers)) {
//            peers.clear()
//            peers.addAll(it.deviceList)
//            deviceNameArray = ArrayList()
//            deviceArray = ArrayList()
//            for (device in it.deviceList) {
//                deviceNameArray.add(device.deviceName)
//                deviceArray.add(device)
//            }
//        }
//    }
//
//    val connectionInfoListener = WifiP2pManager.ConnectionInfoListener {
//        val groupOwnerAddress = it.groupOwnerAddress
//        if (it.groupFormed && it.isGroupOwner) {
//            isHost = true
//            serverClass = ServerClass(this)
//            serverClass.start()
//        } else if (it.groupFormed) {
//            isHost = false
//            clientClass = ClientClass(groupOwnerAddress)
//            clientClass.start()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        requireActivity().registerReceiver(receiver, intentFilter)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        requireActivity().unregisterReceiver(receiver)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        fragmentView = inflater.inflate(R.layout.fragment_connect_device, container, false)
//
//        initializeWork()
//
//        fragmentExecutables()
//
//        return fragmentView
//    }
//
//
//    fun navigateDirForward(
//        position: Int?,
//        recyclerAdapter: RecyclerAdapter,
//        context: Context,
//        filePath: String,
//        function: ((fileModel: FileModel) -> Unit)? = null
//    ): String {
//        this.filePath =
//            super.navigateDirectoryForward(position, recyclerAdapter, context, filePath) {
//                if (function != null) {
//                    function(it)
//                }
//            }
//        navbarRecyclerView(
//            navbarRecyclerAdapter, this.filePath, rootDir, this.recyclerAdapter, context
//        )
//        return this.filePath
//    }
//
//    override fun navigateDirectoryBackward(
//        recyclerAdapter: RecyclerAdapter,
//        rootPath: String,
//        filePath: String
//    ): String {
//        this.filePath = super.navigateDirectoryBackward(recyclerAdapter, rootPath, filePath)
//        navbarRecyclerView(
//            navbarRecyclerAdapter, this.filePath, rootDir, this.recyclerAdapter, requireContext()
//        )
//        return this.filePath
//    }
//
//
//    private fun initializeWork() {
//        rootDir = Gson().fromJson(
//            requireArguments().getString(Const.FRAGMENT_DATA_KEY),
//            StorageDataClass::class.java
//        )
//
//        idBtnConnectDevice = fragmentView.findViewById(R.id.id_btn_connect_device)
//        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
//        idTvLocalStorage = fragmentView.findViewById(R.id.id_tv_local_storage)
//        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
//        idBtnConnectDevice = fragmentView.findViewById(R.id.id_btn_connect_device)
//        idTvSdStorage = fragmentView.findViewById<TextView?>(R.id.id_tv_sd_storage).apply {
//            if (!Tools.isExternalStorageAvailable(requireActivity())) {
//                // the device does not have an SD card
//                visibility = View.GONE
//            }
//        }
//
//        recyclerAdapter = RecyclerAdapter(
//            requireContext(),
//            idRvRootFolder,
//            R.layout.explorer_item,
//            menuVisibility = false
//        )
//        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)
//        filePath = rootDir.rootDirectory
//        filePath = navigateDirForward(null, recyclerAdapter, requireContext(), filePath)
//
//        deviceNameArray = ArrayList()
//        manager = requireActivity().getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
//        channel = manager.initialize(requireContext(), Looper.getMainLooper(), null)
//        wifiManager =
//            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        receiver = MyBroadcastReceiver(manager, channel, this)
//        intentFilter = IntentFilter().apply {
//            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
//        }
//    }
//
//    private fun fragmentExecutables() {
//
//        // this makes sure pressing the back button only exit
//        // this fragment when user is at root directory
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
//            if (filePath.isEmpty()) {
//                isEnabled = false
//                requireActivity().onBackPressed()
//            }
//        }
//        requireActivity().findViewById<TextView>(R.id.id_tv_toolbar).apply {
//            text = getToolbarName(null, requireActivity(), "Connect Device")
//            setOnClickListener {
//                filePath =
//                    navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
//                if (filePath.isEmpty()) {
//                    requireActivity().onBackPressed()
//                }
//            }
//        }
//
//
//        inflateMenuItem()
//
//        connectDeviceButtonClicked()
//
//        rootDirectoryNavigation()
//
//        recyclerViewClickListener()
//    }
//
//    private fun connectDeviceButtonClicked() {
//        idBtnConnectDevice.setOnClickListener {
//            when {
//                isHost != null -> {
//                    val executor = Executors.newSingleThreadExecutor()
//                    executor.execute {
//                        if (isHost == true) {
//                            serverClass.write("message from Host".toByteArray())
//                        } else if (isHost == false) {
//                            clientClass.write("message from Client".toByteArray())
//                        }
//                    }
//                }
//                Tools.isPermissionGranted(requireContext(), Const.FINE_LOCATION_PERMISSION) -> {
//                    if (!Tools.isLocationEnabled(requireContext())) {
//                        getLocationResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                    }
//                    if (Tools.isHotspotOn(requireActivity())) {
//                        requireActivity().popUpWindow(
//                            title = "Notice",
//                            message = "Your wifi - hotspot is on, do disable it"
//                        ) {
//                            it.setPositiveButton("disable hotspot") { _, _ ->
//                                Tools.connectHotspot(requireActivity())
//                            }
//                            it.setNegativeButton("cancel") { s, _ -> s.dismiss() }
//                        }
//                    }
//                    if (!wifiManager.isWifiEnabled) {
//                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
//                        getWifiResult.launch(intent)
//                        return@setOnClickListener
//                    } else {
//                        deviceConnectionPopup()
//                        idBtnConnectDevice.visibility = View.GONE
//                    }
//                }
//                shouldShowRequestPermissionRationale(Const.FINE_LOCATION_PERMISSION) -> {
//                    requireActivity().popUpWindow(
//                        title = "Permission",
//                        message = "Permissions 🙉 are needed for this app 📳 to run successfully"
//                    ) {
//                        it.setCancelable(true)
//                        it.setPositiveButton("Grant permission") { _, _ ->
////                            requestPermission.launch(Const.FINE_LOCATION_PERMISSION)
//                            Tools.askForPermission(
//                                requireActivity(),
//                                Const.FINE_LOCATION_PERMISSION
//                            )
//                        }
//                    }
//                }
//                else -> {
//                    Tools.askForPermission(requireActivity(), Const.FINE_LOCATION_PERMISSION)
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun deviceConnectionPopup() {
//        requireActivity().locationPopUpWindow(
//            fragmentView = fragmentView,
//            R.layout.device_connection_popup,
//        ) { v, p ->
//            p.setOnDismissListener {
//                idBtnConnectDevice.visibility = View.VISIBLE
//            }
//            val connection_status = v.findViewById<TextView>(R.id.id_tv_connect_status)
//            val lv_device = v.findViewById<ListView>(R.id.id_lv_devices)
//            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    connection_status.setBackgroundColor(Color.LTGRAY)
//                    connection_status.text =
//                        requireActivity().getString(R.string.format_string, "Discovery Started")
//                }
//
//                override fun onFailure(p0: Int) {
//                    connection_status.setBackgroundColor(Color.DKGRAY)
//                    connection_status.text =
//                        requireActivity().getString(R.string.format_string, "Discovery Failed")
//                }
//            })
//            v.findViewById<Button>(R.id.id_btn_discover_devices).setOnClickListener {
//                lv_device.adapter = ArrayAdapter(
//                    requireContext(),
//                    R.layout.list_item,
//                    R.id.id_tv_list_item,
//                    deviceNameArray
//                )
//            }
//            lv_device.setOnItemClickListener { parent, view, position, id ->
//                val device = deviceArray[position]
//                val config = WifiP2pConfig()
//                config.deviceAddress = device.deviceAddress
//                manager.connect(channel, config, object : WifiP2pManager.ActionListener {
//                    override fun onSuccess() {
//                        connection_status.text = requireActivity().getString(
//                            R.string.format_string,
//                            "Connected to ${device.deviceName}"
//                        )
//                        p.dismiss()
//                        idBtnConnectDevice.text =
//                            requireActivity().getString(R.string.format_string, "Send")
//                    }
//
//                    override fun onFailure(p0: Int) {
//                        connection_status.text = requireActivity().getString(
//                            R.string.format_string,
//                            "Not connected to ${device.deviceName}"
//                        )
//                    }
//
//                })
//            }
//        }
//    }
//
//    private fun rootDirectoryNavigation() {
//        idTvLocalStorage.setOnClickListener {
//            rootDir = StorageDataClass(
//                rootDirectory = Const.ROOT_PATH,
//                isSdStorage = false
//            )
//            filePath = navigateDirForward(
//                null,
//                recyclerAdapter,
//                requireContext(),
//                rootDir.rootDirectory
//            )
//        }
//
//        idTvSdStorage.setOnClickListener {
//            rootDir = StorageDataClass(
//                rootDirectory = Tools.getExternalSDCardRootDirectory(requireActivity())!!,
//                isSdStorage = true
//            )
//            filePath = navigateDirForward(
//                null,
//                recyclerAdapter,
//                requireContext(),
//                rootDir.rootDirectory
//            )
//        }
//    }
//
//    private fun recyclerViewClickListener() {
//        recyclerAdapter.onClickListener(object : OnClickInterface {
//            override fun onItemClick(position: Int, view: View) {
//                filePath = navigateDirForward(
//                    position,
//                    recyclerAdapter,
//                    requireContext(),
//                    filePath
//                ) {
//                    Tools.debugMessage("shearing file")
//                    val executor = Executors.newSingleThreadExecutor()
//                    executor.execute {
//                        if (isHost == true) {
//                            serverClass.write(ByteArray(4024))
//                        } else if (isHost == false) {
//                            clientClass.write(ByteArray(4024))
//                        }
//                    }
//                }
//            }
//        })
//    }
//
//    private fun inflateMenuItem() {
//        navbarMenuProvider(
//            requireActivity(),
//            null,
//            showComputerIcon = true,
//            showDeviceIcon = false
//        ) {
//            when (it.itemId) {
//                R.id.id_menu_computer -> {
//                    Tools.navigateFragmentToFragment(
//                        requireActivity(),
//                        R.id.fragmentContainerView,
//                        R.id.connectPcFragment
//                    )
//                    true
//                }
//                else -> false
//            }
//        }
//
//    }
//}


//    // REQUEST PERMISSION
//    private val requestPermission =
//        registerForActivityResult(
//            RequestPermission()
//        ) {}
//
//    // WIFI Receiver Contract
//    private val getWifiResult =
//        registerForActivityResult(
//            StartActivityForResult()
//        ) {
//            if (wifiManager.isWifiEnabled) {
//                Tools.showToast(requireContext(), "Permission was granted, successfully")
//            } else {
//                requireActivity().popUpWindow(
//                    title = "enable wifi",
//                    message = "Wifi is needed for ${Const.APP_NAME} to work"
//                ) { it.setCancelable(true) }
//            }
//        }
//
//    // LOCATION Launcher
//    private val getLocationResult =
//        registerForActivityResult(
//            StartActivityForResult()
//        ) {}
//
//    // Listen For Peers
//    val peerListListener = WifiP2pManager.PeerListListener { peerList ->
//        if (peerList.deviceList != peers) {
//            peers.clear()
//            peers.addAll(peerList.deviceList)
//            deviceNameArray = ArrayList()
//            deviceArray = ArrayList()
//
//            for (device in peerList.deviceList) {
//                deviceNameArray.add(device.deviceName)
//                deviceArray.add(device)
//            }
//        }
//    }
//
//    // Create connection info (click) listener
//    val connectionInfoListener = WifiP2pManager.ConnectionInfoListener { wifiP2pInfo ->
//        val groupOwnerAddress = wifiP2pInfo.groupOwnerAddress
//        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
//            serverClass = ServerClass(this)
//            serverClass.start()
//        } else if (wifiP2pInfo.groupFormed) {
//            clientClass = ClientClass(this, groupOwnerAddress)
//            clientClass.start()
//        }
//    }
//
//    val handler = Handler(Looper.getMainLooper(), Handler.Callback { msg ->
//        when (msg.what) {
//            Const.MESSAGE_CODE -> {
//                val readBuff = msg.obj as ByteArray
//                val tempMsg = String(readBuff, 0, msg.arg1)
//                Tools.debugMessage(tempMsg, "TEMP-MESSAGE(OHIS)")
//            }
//        }
//        return@Callback true
//    })
//
//    override fun onResume() {
//        super.onResume()
//        requireActivity().registerReceiver(mReceiver, mIntentFilter)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        requireActivity().unregisterReceiver(mReceiver)
//    }
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        // Inflate the layout for this fragment
//        fragmentView = inflater.inflate(R.layout.fragment_connect_device, container, false)
//
//        fragmentInitializers()
//
//        fragmentExecutables()
//
//        return fragmentView
//    }
//
//
//    override fun navigateDirectoryForward(
//        position: Int?,
//        recyclerAdapter: RecyclerAdapter,
//        context: Context,
//        filePath: String
//    ): String {
//        this.filePath = super.navigateDirectoryForward(position, recyclerAdapter, context, filePath)
//        navbarRecyclerView(
//            navbarRecyclerAdapter, this.filePath, rootDir, this.recyclerAdapter, context
//        )
//        return this.filePath
//    }
//
//    override fun navigateDirectoryBackward(
//        recyclerAdapter: RecyclerAdapter,
//        rootPath: String,
//        filePath: String
//    ): String {
//        this.filePath = super.navigateDirectoryBackward(recyclerAdapter, rootPath, filePath)
//        navbarRecyclerView(
//            navbarRecyclerAdapter, this.filePath, rootDir, this.recyclerAdapter, requireContext()
//        )
//        return this.filePath
//    }
//
//    private fun fragmentInitializers() {
//        rootDir = Gson().fromJson(
//            requireArguments().getString(Const.FRAGMENT_DATA_KEY),
//            StorageDataClass::class.java
//        )
//        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
//        idTvLocalStorage = fragmentView.findViewById(R.id.id_tv_local_storage)
//        idTvSdStorage = fragmentView.findViewById(R.id.id_tv_sd_storage)
//        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
//        idBtnConnectDevice = fragmentView.findViewById(R.id.id_btn_connect_device)
//        if (!Tools.isExternalStorageAvailable()){idTvSdStorage.visibility = View.GONE}
//        sendReceive = ShareDataThread(this, null)
//        recyclerAdapter = RecyclerAdapter(
//            requireContext(),
//            idRvRootFolder,
//            R.layout.explorer_item,
//            menuVisibility = false
//        )
//        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)
//        filePath = rootDir.rootDirectory
//        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
//
//        wifiManager =
//            requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        mManager =
//            requireActivity().applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
//        mChannel = mManager.initialize(requireContext(), Looper.getMainLooper(), null)
//
//        deviceNameArray = ArrayList()
//
//        mReceiver = MyBroadcastReceiver(mManager = mManager, mChannel = mChannel, mActivity = this)
//
//        mIntentFilter = IntentFilter().apply {
//            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
//            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
//        }
//    }
//
//    private fun fragmentExecutables() {
//        // this makes sure pressing the back button only exit
//        // this fragment when user is at root directory
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
//            if (filePath.isEmpty()) {
//                isEnabled = false
//                requireActivity().onBackPressed()
//            }
//        }
//        requireActivity().findViewById<TextView>(R.id.id_tv_toolbar).apply {
//            text = getToolbarName(null, requireActivity(), "Connect Device")
//            setOnClickListener {
//                filePath =
//                    navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
//                if (filePath.isEmpty()) {
//                    requireActivity().onBackPressed()
//                }
//            }
//        }
//
//        rootDirectoryNavigation()
//
//        recyclerViewClickListener()
//
//        inflateMenuItem()
//
//        connectDeviceButton()
//    }
//
//    private fun rootDirectoryNavigation() {
//        idTvLocalStorage.setOnClickListener {
//            rootDir = StorageDataClass(
//                rootDirectory = Const.ROOT_PATH,
//                isSdStorage = false
//            )
//            filePath = navigateDirectoryForward(
//                null,
//                recyclerAdapter,
//                requireContext(),
//                rootDir.rootDirectory
//            )
//        }
//
//        idTvSdStorage.setOnClickListener {
//            rootDir = StorageDataClass(
//                rootDirectory = Tools.getExternalSDCardRootDirectory(requireActivity())!!,
//                isSdStorage = true
//            )
//            filePath = navigateDirectoryForward(
//                null,
//                recyclerAdapter,
//                requireContext(),
//                rootDir.rootDirectory
//            )
//        }
//    }
//
//    private fun connectDeviceButton() {
//        idBtnConnectDevice.setOnClickListener {
//            when {
//                Tools.isPermissionGranted(requireContext(), Const.FINE_LOCATION_PERMISSION) -> {
//                    if (!Tools.isLocationEnabled(requireContext())) {
//                        getLocationResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
//                    }
//                    if (!wifiManager.isWifiEnabled) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                            // Caller
//                            val intent = Intent(Settings.Panel.ACTION_WIFI)
//                            getWifiResult.launch(intent)
//                        } else {
//                            wifiManager.isWifiEnabled = !wifiManager.isWifiEnabled
//                        }
//                    }
//                    if (Tools.isHotspotOn(requireActivity())) {
//                        requireActivity().popUpWindow(
//                            title = "Notice",
//                            message = "Your wifi - hotspot is on, do disable it"
//                        ) {
//                            it.setPositiveButton("disable hotspot") { _, _ ->
//                                Tools.connectHotspot(requireActivity())
//                            }
//                            it.setNegativeButton("cancel") { s, _ -> s.dismiss() }
//                        }
//
//                    } else {
//                        deviceConnectionPopup()
//                        idBtnConnectDevice.visibility = View.GONE
//                    }
//                }
//                shouldShowRequestPermissionRationale(Const.FINE_LOCATION_PERMISSION) -> {
//                    requireActivity().popUpWindow(
//                        title = "Permission",
//                        message = "Permissions 🙉 are needed for this app 📳 to run successfully"
//                    ) {
//                        it.setCancelable(true)
//                        it.setPositiveButton("Grant permission") { _, _ ->
//                            requestPermission.launch(Const.FINE_LOCATION_PERMISSION)
//                        }
//                    }
//                }
//                else -> {
//                    requestPermission.launch(Const.FINE_LOCATION_PERMISSION)
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun deviceConnectionPopup() {
//        requireActivity().locationPopUpWindow(
//            fragmentView = fragmentView,
//            R.layout.device_connection_popup,
//        ) { v, p ->
//            p.setOnDismissListener {
//                idBtnConnectDevice.visibility = View.VISIBLE
//            }
//            val itcs = v.findViewById<TextView>(R.id.id_tv_connect_status)
//            mManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
//                override fun onSuccess() {
//                    itcs.setBackgroundColor(Color.LTGRAY)
//                    itcs.text =
//                        requireActivity().getString(R.string.format_string, "Discovery Started")
//                }
//
//                override fun onFailure(p0: Int) {
//                    itcs.setBackgroundColor(Color.DKGRAY)
//                    itcs.text =
//                        requireActivity().getString(R.string.format_string, "Discovery Failed")
//                }
//            })
//            val ild = v.findViewById<ListView>(R.id.id_lv_devices)
//            v.findViewById<Button>(R.id.id_btn_discover_devices).setOnClickListener {
//                ild.adapter =
//                    ArrayAdapter(
//                        requireContext(),
//                        R.layout.list_item,
//                        R.id.id_tv_list_item,
//                        deviceNameArray
//                    )
//            }
//            ild.setOnItemClickListener { _, _, position, _ ->
//                val device = deviceArray[position]
//                val config = WifiP2pConfig()
//                config.deviceAddress = device.deviceAddress
//                mManager.connect(mChannel, config, object : WifiP2pManager.ActionListener {
//                    override fun onSuccess() {
//                        CoroutineScope(Dispatchers.IO).launch {
//                            sendReceive.writeBytes("device ${device.deviceName} sent message Ohiorenua".toByteArray())
//                        }
//                    }
//
//                    override fun onFailure(p0: Int) {
//                        Tools.showToast(
//                            requireContext(),
//                            "Could not connected to ${device.deviceName}"
//                        )
//                    }
//                })
//            }
//        }
//    }
//
//    private fun recyclerViewClickListener() {
//        recyclerAdapter.onClickListener(object : OnClickInterface {
//            override fun onItemClick(position: Int, view: View) {
//                filePath =
//                    navigateDirectoryForward(position, recyclerAdapter, requireContext(), filePath)
//            }
//        })
//    }
//
//    private fun inflateMenuItem() {
//        navbarMenuProvider(
//            requireActivity(),
//            null,
//            showComputerIcon = true,
//            showDeviceIcon = false
//        ) {
//            when (it.itemId) {
//                R.id.id_menu_computer -> {
//                    Tools.navigateFragmentToFragment(
//                        this@ConnectDeviceFragment,
//                        R.id.connectPcFragment
//                    )
//                    true
//                }
//                else -> false
//            }
//        }
//
//    }
//}
