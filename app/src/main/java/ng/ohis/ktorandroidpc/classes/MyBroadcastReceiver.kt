package ng.ohis.ktorandroidpc.classes

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
import androidx.appcompat.app.AppCompatActivity
import ng.ohis.ktorandroidpc.MainActivity
import ng.ohis.ktorandroidpc.fragments.ConnectDeviceFragment
import ng.ohis.ktorandroidpc.utills.Tools

class MyBroadcastReceiver(
    private val mManager: WifiP2pManager,
    private val mChannel: WifiP2pManager.Channel?,
    private val generateActivity: ConnectDeviceFragment?,
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
    }

}
