package ng.ohis.ktorandroidpc.classes

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.fragments.ConnectDeviceFragment
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools


class MyBroadcastReceiver(
    private val mManager: WifiP2pManager,
    private val mChannel: WifiP2pManager.Channel?,
    private val mActivity: ConnectDeviceFragment
) : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action){
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION ->{
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Tools.showToast(context, "Wifi is enabled")
                } else context.let { Tools.showToast(it, "Wifi is dis-enabled") }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION ->{
                if (Tools.isPermissionGranted(context, Const.FINE_LOCATION_PERMISSION)){
                    mManager.requestPeers(mChannel, mActivity.peerListListener)
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION->{
                val networkInfo = isNetworkAvailable(context)
                if (networkInfo){
                    mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener)
                }
                else{
                    Tools.showToast(mActivity.requireContext(), mActivity.getString(R.string.formatted_string, "Device disconnected"))
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION->{

            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                // for check for wifi p2p
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
//            return connectivityManager.isActiveNetworkMetered
        }
    }
}
