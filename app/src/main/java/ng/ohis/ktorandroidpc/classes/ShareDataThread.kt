package ng.ohis.ktorandroidpc.classes

import ng.ohis.ktorandroidpc.fragments.ConnectDeviceFragment
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.IOException
import java.net.Socket

class ShareDataThread(
    private val connectDeviceFragment: ConnectDeviceFragment,
    private val socket: Socket?
) : Thread() {
    private val inputStream = socket?.getInputStream()
    private val outputStream = socket?.getOutputStream()

    override fun run() {
        val buffer = ByteArray(4024)
        var byte: Int
        while (socket != null) {
            try {
                byte = inputStream?.read(buffer)!!
                if (byte > 0) {
                    connectDeviceFragment.handler.obtainMessage(
                        Const.MESSAGE_CODE,
                        byte,
                        -1,
                        buffer
                    ).sendToTarget()
                }
            } catch (io: IOException) {
                Tools.debugMessage(io.message.toString(), io.cause.toString())
            }
        }
    }

    fun writeBytes(byteArray: ByteArray) {
        outputStream?.write(byteArray)
    }
}
