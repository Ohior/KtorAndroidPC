package com.example.ktorandroidpc

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.ktorandroidpc.utills.Const
import io.ktor.util.reflect.*

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }


    override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
        if (key == "appTheme") {
            val appTheme = pref?.getString(key, "1")
            switchTheme(appTheme)
        }
    }


    companion object {
        private fun switchTheme(appTheme:String?){
            when (appTheme?.toInt()) {
                1 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                2 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                3 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                4 -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
        fun appSettings(activity: Activity) {
            val preferenceManager = PreferenceManager.getDefaultSharedPreferences(activity)
            val appTheme = preferenceManager.getString("appTheme","1")
            switchTheme(appTheme)

            val downloadFolder = preferenceManager.getBoolean("downloadFolder", false)
            if (downloadFolder) {
                Const.OH_TRANSFER_PATH = Const.ROOT_PATH + "/Download/"
            } else {
                Const.OH_TRANSFER_PATH = "${Const.ROOT_PATH}/${Const.OH_TRANSFER}/"
            }
        }
    }

}
