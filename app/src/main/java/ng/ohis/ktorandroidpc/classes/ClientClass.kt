package ng.ohis.ktorandroidpc.classes

import android.os.Handler
import android.os.Looper
import ng.ohis.ktorandroidpc.utills.Const
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.Executors


class ClientClass(
    private val inetAddress: InetAddress
) : Thread() {
    private lateinit var socket: Socket
    private lateinit var inputStream: FileInputStream
    private lateinit var outputStream: DataOutputStream

    override fun run() {
        socket = Socket(inetAddress, Const.SERVER_PORT)
    }

    fun writeFile(file: File, function: (size: Long, count: Int, name: String) -> Unit) {
        inputStream = FileInputStream(file)
        outputStream = DataOutputStream(socket.getOutputStream())

        val fileName = file.name
        val fileNameByteArray = fileName.toByteArray()
        outputStream.writeInt(fileNameByteArray.size)
        outputStream.write(fileNameByteArray)

        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executor.execute {
            val byteArray = ByteArray(4 * 1024)
            var bytes: Int = inputStream.read(byteArray)
            var count = 0
            while (bytes > 0) {
                count += bytes
                outputStream.write(byteArray, 0, bytes)
                bytes = inputStream.read(byteArray)
                function(file.length(), count, fileName)
            }
        }
        inputStream.close()
        outputStream.close()
        socket.close()
    }
}


// WORKING STEP ONE(1)
//class ClientClass(
//    private val context: Context,
//    private val inetAddress: InetAddress
//) : Thread() {
//    private lateinit var socket: Socket
//    //    private var inputStream: InputStream? = null
//    private var inputStream: FileInputStream? = null
//    private var outputStream: DataOutputStream? = null
//
//    override fun run() {
//        try {
////            socket.bind(null)
////            socket.connect(InetSocketAddress(inetAddress, Const.SERVER_PORT), 500)
//            socket = Socket(inetAddress, Const.SERVER_PORT)
//            Tools.debugMessage("Client socket - " + socket.isConnected)
//        } catch (e: IOException) {
//            Tools.debugMessage("an error occurred", e.cause.toString())
//        }
//    }
//
//
//    fun writeFile(file: File) {
//        inputStream = FileInputStream(file)
//        outputStream = DataOutputStream(socket.getOutputStream())
//        val fileName = file.name
//
//        val executor = Executors.newSingleThreadExecutor()
//        val handler = Handler(Looper.getMainLooper())
//        executor.execute {
//            val fileContentByteArray = ByteArray(file.length().toInt())
//            val fileNameBytes = fileName.toByteArray()
//            var bytes: Int
//            inputStream!!.read(fileContentByteArray)
//
//            outputStream!!.writeInt(fileNameBytes.size)
//            outputStream!!.write(fileNameBytes)
//
//            outputStream!!.writeInt(fileContentByteArray.size)
//            outputStream!!.write(fileContentByteArray)
//
//            // Close the streams and the socket
////            inputStream!!.close()
////            outputStream?.close()
////            socket.close()
//        }
//
//    }
//}