package com.example.ktorandroidpc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ktorandroidpc.databinding.ActivityMainBinding
import com.example.ktorandroidpc.plugins.configureRouting
import com.example.ktorandroidpc.plugins.configureTemplating
import com.example.ktorandroidpc.utills.Const
import com.example.ktorandroidpc.utills.Tools
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.freemarker.*
import io.ktor.server.netty.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var coroutineScope: CoroutineScope
    private var connectOrDisconnect = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Initiallizers()

        ClickListener()

        coroutineScope.launch {
        embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
            configureRouting(this, applicationContext)
            configureTemplating(this)
        }.start(wait = false)
        }

    }

    private fun ClickListener() {
        binding.idBtnConnect2browser.setOnClickListener {
            if (connectOrDisconnect) {
                coroutineScope.launch {
                    embeddedServer(Netty, port = Const.PORT, host = Const.ADDRESS) {
                        configureRouting(this)
                    }.start(wait = false)
                }
            } else {
                coroutineScope.cancel()
                Tools.showToast(application, "Connection Disabled")
            }
            connectOrDisconnect = !connectOrDisconnect

        }
    }

    private fun Initiallizers() {
        coroutineScope = CoroutineScope(Dispatchers.IO)
    }
}
