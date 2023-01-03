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
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

//class ServerClass(private var connectDeviceFragment: ConnectDeviceFragment):Thread() {
//    private lateinit var socket: Socket
//    private lateinit var serverSocket: ServerSocket
//    override fun run() {
//        try {
//            serverSocket = ServerSocket(Const.SERVER_PORT)
//            socket = serverSocket.accept()
//            connectDeviceFragment.sendReceive = SendReceiveThread(connectDeviceFragment, socket)
//            connectDeviceFragment.sendReceive.start()
//        }catch (io:IOException){
//            Tools.debugMessage(io.message.toString(), io.cause.toString())
//        }
//    }
//}

class ServerClass(
    private val connectDeviceFragment: ConnectDeviceFragment,
) : Thread() {
    private lateinit var serverSocket: ServerSocket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private var socket: Socket? = null

    override fun run() {
        try {
            serverSocket = ServerSocket(Const.SERVER_PORT)
            socket = serverSocket.accept()
            inputStream = socket!!.getInputStream()
            outputStream = socket!!.getOutputStream()
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
                    bytes = inputStream.read(buffer)
                    if (bytes > 0) {
                        val finalByte = bytes
                        handler.post {
                            val tempMsg = String(buffer, 0, finalByte)
                            Tools.debugMessage(tempMsg)
                        }
                    }
                } catch (io: IOException) {
                    Tools.debugMessage(io.message.toString(), io.cause.toString())
                }
            }
        }
    }

    fun write(byteArray: ByteArray) {
        try {
            outputStream.write(byteArray)
        } catch (io: IOException) {
            Tools.debugMessage(io.message.toString(), io.cause.toString())
        }
    }
}