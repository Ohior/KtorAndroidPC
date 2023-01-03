package ng.ohis.ktorandroidpc.classes

import android.os.Environment
import android.os.Handler
import android.os.Looper
import ng.ohis.ktorandroidpc.fragments.ConnectDeviceFragment
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.Executors

//class ClientClass(
//    private val connectDeviceFragment: ConnectDeviceFragment,
//    private val insetAddress: InetAddress
//) : Thread() {
//    private val socket = Socket()
//
//    override fun run() {
//        try {
//            socket.connect(InetSocketAddress(insetAddress.hostAddress, Const.SERVER_PORT), 500)
//            connectDeviceFragment.sendReceive = SendReceiveThread(connectDeviceFragment, socket)
//            connectDeviceFragment.sendReceive.start()
//        } catch (io: IOException) {
//            Tools.debugMessage(io.message.toString(), io.cause.toString())
//        }
//    }
//}

class ClientClass(
    private val inetAddress: InetAddress
) : Thread() {

    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    override fun run() {
        try {
            socket = Socket()
            socket?.connect(InetSocketAddress(inetAddress, Const.SERVER_PORT), 500)
            inputStream = socket?.getInputStream()
            outputStream = socket?.getOutputStream()
        } catch (io: IOException) {
            Tools.debugMessage(io.message.toString(), io.cause.toString())
        }

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val buffer = ByteArray(4024)
            var bytes: Int
            while (socket != null) {
                try {
                    bytes = inputStream!!.read(buffer)
                    if (bytes > 0){
                        val finalByte = bytes
                        handler.post {
                            val tempMsg = String(buffer, 0, finalByte)
                            Tools.debugMessage(tempMsg)
                        }
                    }
                }catch (io:IOException){
                    Tools.debugMessage(io.message.toString(), io.cause.toString())
                }
            }
        }
    }

    fun write(byteArray: ByteArray) {
        try {
            outputStream?.write(byteArray)
        } catch (io: IOException) {
            Tools.debugMessage(io.message.toString(), io.cause.toString())
        }
    }
}