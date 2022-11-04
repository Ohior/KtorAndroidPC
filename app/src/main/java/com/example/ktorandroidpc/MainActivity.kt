package com.example.ktorandroidpc

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.ktorandroidpc.fragments.ExplorerFragment
import com.example.ktorandroidpc.utills.*
import io.ktor.util.reflect.*
import java.lang.reflect.Method


class MainActivity : AppCompatActivity() {


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Const.PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            return
        } else {
            Tools.requestForAllPermission(this@MainActivity)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        baseContext.cacheDir.deleteRecursively()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SettingsActivity.appSettings(this)
        setSupportActionBar(findViewById(R.id.id_toolbar))
//        val customTitle = findViewById<TextView>(R.id.id_tv_toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val m: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible", java.lang.Boolean.TYPE
                )
                m.isAccessible = true
                m.invoke(menu, true)
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return super.onMenuOpened(featureId, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.id_menu_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
