package ng.ohis.ktorandroidpc.classes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import ng.ohis.ktorandroidpc.MainActivity
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

            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION ->{
                if (Tools.isPermissionGranted(context, Const.FINE_LOCATION_PERMISSION)){
                    mManager.requestPeers(mChannel, mActivity.peerListListener)
                    Tools.debugMessage("peers listener")
                }
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION->{

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
        }
    }


}
