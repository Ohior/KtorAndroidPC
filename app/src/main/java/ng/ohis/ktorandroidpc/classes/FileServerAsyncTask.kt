package ng.ohis.ktorandroidpc.classes

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import ng.ohis.ktorandroidpc.adapter.FileModel
import ng.ohis.ktorandroidpc.fragments.ConnectDeviceFragment
import ng.ohis.ktorandroidpc.utills.Const
import java.io.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket


class FileServerAsyncTask(
    private val connectDeviceFragment: ConnectDeviceFragment,
    private val fileModel: FileModel,
    private val inetAddress: InetAddress
) : Thread() {
    private lateinit var serverSocket: ServerSocket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream
    private var socket: Socket? = null

    override fun run() {
        serverSocket = ServerSocket(Const.SERVER_PORT)
        /**
         * Wait for client connections. This call blocks until a
         * connection is accepted from a client.
         */
        val client = serverSocket.accept()
        /**
         * If this code is reached, a client has connected and transferred data
         * Save the input stream from the client as a JPEG file
         */
        inputStream = client.getInputStream()
        copyFile(inputStream, FileOutputStream(fileModel.file))
        serverSocket.close()
    }

    private fun copyFile(inputStream: InputStream, fileOutputStream: FileOutputStream) {
        val context = connectDeviceFragment.context
        val host: String
        val port: Int
        var len: Int
        val socket = Socket()
        val buf = ByteArray(1024)
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket.bind(null)
            socket.connect((InetSocketAddress(inetAddress, Const.SERVER_PORT)), 500)

            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data is retrieved by the server device.
             */
            val outputStream = socket.getOutputStream()
            val cr = context?.contentResolver
//            val inputStream: InputStream? = cr.openInputStream(Uri.parse("path/to/picture.jpg"))
            while (inputStream.read(buf).also { len = it } != -1) {
                fileOutputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: FileNotFoundException) {
            //catch logic
        } catch (e: IOException) {
            //catch logic
        } finally {
            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            socket.takeIf { it.isConnected }?.apply {
                close()
            }
        }
    }
}


//class FileServerAsyncTask(
//    private val context: Context,
//    private var fileModel: FileModel
//): AsyncTask<Void, Void, String?>() {
//    override fun doInBackground(vararg params: Void?): String? {
//        val serverSocket = ServerSocket(Const.SERVER_PORT)
//        return serverSocket.use{
//            /**
//             * Wait for client connections. This call blocks until a
//             * connection is accepted from a client.
//             */
//            val client = serverSocket.accept()
//            /**
//             * If this code is reached, a client has connected and transferred data
//             * Save the input stream from the client as a JPEG file
//             */
//
//            val f = fileModel.file
//            val dirs = File(f.toString())
//            dirs.takeIf { it.doesNotExist() }?.apply {
//                mkdirs()
//            }
//            f.createNewFile()
//            val inputStream = client.getInputStream()
//            copyFile(inputStream, FileOutputStream(f))
//            serverSocket.close()
//            f.absolutePath
//        }
//    }
//
//    private fun File.doesNotExist(): Boolean = !exists()
//    /**
//     * Start activity that can handle the JPEG image
//     */
//    override fun onPostExecute(result: String?) {
//        result?.run {
//            statusText.text = "File copied - $result"
//            val intent = Intent(android.content.Intent.ACTION_VIEW).apply {
//                setDataAndType(Uri.parse("file://$result"), "image/*")
//            }
//            context.startActivity(intent)
//        }
//    }
//
//}