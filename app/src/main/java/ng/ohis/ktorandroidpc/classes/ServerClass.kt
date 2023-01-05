package ng.ohis.ktorandroidpc.classes

import android.os.Handler
import android.os.Looper
import ng.ohis.ktorandroidpc.utills.Const
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors


class ServerClass : Thread() {
    private lateinit var serverSocket: ServerSocket
    private lateinit var socket: Socket
    private lateinit var inputStream: DataInputStream
    private lateinit var outputStream: FileOutputStream

    override fun run() {
        serverSocket = ServerSocket(Const.SERVER_PORT)
        socket = serverSocket.accept()
        var hasConnected = socket.isConnected
        while (!hasConnected) {
            socket = serverSocket.accept()
            hasConnected = socket.isConnected
        }
    }

    fun writeFile(file: File, function: (size: Long, count: Int, name: String) -> Unit) {
        inputStream = DataInputStream(socket.getInputStream())

        val fileNameLength = inputStream.readInt()
        if (fileNameLength > 0) {
            val filNameBytes = ByteArray(fileNameLength)
            inputStream.readFully(filNameBytes, 0, filNameBytes.size)
            val fileName = String(filNameBytes)

            val fileToDownload = File(Const.DOWNLOAD_DIR + fileName)
            outputStream = FileOutputStream(fileToDownload)
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
}

// WORKING STEP ONE(1)
//class ServerClass : Thread() {
//
//    private lateinit var socket: Socket
//    private lateinit var serverSocket: ServerSocket
//    private var inputStream: DataInputStream? = null
//    private var outputStream: FileOutputStream? = null
//
//    override fun run() {
//        serverSocket = ServerSocket(Const.SERVER_PORT)
//        val executor = Executors.newSingleThreadExecutor()
//        val handler = Handler(Looper.getMainLooper())
//        executor.execute {
//            while (true) {
//                socket = serverSocket.accept()
//                inputStream = DataInputStream(socket.getInputStream())
//                val fileNameLength = inputStream!!.readInt()
//                if (fileNameLength > 0) {
//                    val filNameBytes = ByteArray(fileNameLength)
//                    inputStream!!.readFully(filNameBytes, 0, filNameBytes.size)
//                    val fileName = String(filNameBytes)
//
//                    val fileContentLength = inputStream!!.readInt()
//                    if (fileContentLength > 0) {
//                        val fileContentBytes = ByteArray(fileContentLength)
//                        inputStream!!.readFully(fileContentBytes, 0, fileContentLength)
//
//                        val fileToDownload = File(Const.DOWNLOAD_DIR + fileName)
//
//                        outputStream = FileOutputStream(fileToDownload)
//                        outputStream!!.write(fileContentBytes)
//                    }
//                }
//            }
//        }
//
//    }
//
//    fun writeFile(file: File) {
//
//
//    }
//
//}