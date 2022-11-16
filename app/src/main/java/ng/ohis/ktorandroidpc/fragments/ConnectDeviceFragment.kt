package ng.ohis.ktorandroidpc.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Looper.getMainLooper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.classes.MyBroadcastReceiver
import ng.ohis.ktorandroidpc.utills.Tools
import java.util.concurrent.atomic.AtomicBoolean


class ConnectDeviceFragment : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var idImgQrCode: ImageView

    private lateinit var mManager: WifiP2pManager
    private lateinit var wifiManager: WifiManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private var mReceiver: BroadcastReceiver? = null

    private lateinit var mIntentFilter: IntentFilter
    private lateinit var connected: AtomicBoolean

    override fun onPause() {
        super.onPause()
        if (mReceiver != null) requireActivity().unregisterReceiver(mReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_connect_device, container, false)

        fragmentInitializers()

        return fragmentView
    }

    private fun fragmentInitializers() {
        idImgQrCode = fragmentView.findViewById(R.id.id_img_qr_code)
    }

    fun generateQRCode(text: String): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 512, 512)
            idImgQrCode.setImageBitmap(bitmap)
            bitmap
        } catch (e: WriterException) {
            Tools.debugMessage(e.message.toString(), "WriterException")
            null
        }
    }

    private fun onPermissionsChecked() {
        wifiManager = requireActivity().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mManager = requireActivity().applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        mChannel = mManager.initialize(requireContext(), getMainLooper(), null)
        mReceiver = MyBroadcastReceiver(mManager, mChannel, this)
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        if (Tools.checkAllPermission(requireActivity())) {
            mManager.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    TODO("Not yet implemented")
                }

                override fun onFailure(p0: Int) {
                    TODO("Not yet implemented")
                }
            })
            connected = AtomicBoolean()
            connected.set(false)
        }
        requireActivity().registerReceiver(mReceiver, mIntentFilter)
    }

    fun setWifiOn() {
        wifiManager.isWifiEnabled = true
    }


    val connectionInfoListener: WifiP2pManager.ConnectionInfoListener = WifiP2pManager.ConnectionInfoListener {
        val ownerAddress = it.groupOwnerAddress
        if (!connected.get()) {
            mManager.stopPeerDiscovery(mChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                }

                override fun onFailure(p0: Int) {
                }

            })
            if (it.groupFormed && it.isGroupOwner) {
                CoroutineScope(Dispatchers.IO).launch {
                    // TODO: 15/11/2022  
                }
            } else if (it.groupFormed) {
                CoroutineScope(Dispatchers.IO).launch {
                    // TODO: 15/11/2022
                }
            }
        }
    }

}