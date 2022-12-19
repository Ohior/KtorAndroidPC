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
    }
}