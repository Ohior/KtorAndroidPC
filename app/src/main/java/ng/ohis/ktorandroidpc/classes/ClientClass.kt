package ng.ohis.ktorandroidpc.classes

import ng.ohis.ktorandroidpc.fragments.ConnectDeviceFragment
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class ClientClass(
    private val connectDeviceFragment: ConnectDeviceFragment,
    private val insetAddress: InetAddress
) :
    Thread() {
    private val socket = Socket()
    override fun run() {
        try {
            socket.connect(InetSocketAddress(insetAddress.hostAddress, Const.SERVER_PORT), 500)
            connectDeviceFragment.sendReceive = ShareDataThread(connectDeviceFragment, socket)
            connectDeviceFragment.sendReceive.start()
        } catch (io: IOException) {
            Tools.debugMessage(io.message.toString(), io.cause.toString())
        }
    }
}