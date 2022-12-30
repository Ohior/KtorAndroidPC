package ng.ohis.ktorandroidpc.classes

import ng.ohis.ktorandroidpc.fragments.ConnectDeviceFragment
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class ServerClass(private var connectDeviceFragment: ConnectDeviceFragment):Thread() {
    private lateinit var socket: Socket
    private lateinit var serverSocket: ServerSocket
    override fun run() {
        try {
            serverSocket = ServerSocket(Const.SERVER_PORT)
            socket = serverSocket.accept()
            connectDeviceFragment.sendReceive = ShareDataThread(connectDeviceFragment, socket)
            connectDeviceFragment.sendReceive.start()
        }catch (io:IOException){
            Tools.debugMessage(io.message.toString(), io.cause.toString())
        }
    }
}