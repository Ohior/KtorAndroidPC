package ng.ohis.ktorandroidpc

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.File

//setting activity controls the settings for this app
class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onDestroy() {
        appSettings(this)
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onBackPressed() {
        appSettings(this)
        super.onBackPressed()
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
        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val key = preference.key
            if (key != null && key == "shareApp") {
                shareChransverApk()
            }
            return super.onPreferenceTreeClick(preference)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        private fun shareChransverApk() {
            try {
                val pm = requireActivity().packageManager
                val ai = pm.getApplicationInfo(requireActivity().packageName, 0)
                val file = File(ai.publicSourceDir)
                val uri =
                    FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
                val share = Intent()
                share.action = Intent.ACTION_SEND
                share.type = "application/vnd.android.package-archive"
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                share.putExtra(Intent.EXTRA_STREAM, uri)
                requireActivity().startActivity(share)

            } catch (e: Exception) {
                Tools.debugMessage(e.message.toString())
            }
        }
    }


    override fun onSharedPreferenceChanged(pref: SharedPreferences?, key: String?) {
        if (key == "appTheme") {
            val appTheme = pref?.getString(key, "1")
            switchTheme(appTheme)
        }
    }

    companion object {

        private fun switchTheme(appTheme: String?) {
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
            val appTheme = preferenceManager.getString("appTheme", "1")
            val downloadFolder = preferenceManager.getBoolean("downloadFolder", false)
            val showHiddenFiles = preferenceManager.getBoolean("showHiddenFile", true)
            switchTheme(appTheme)
            Const.SETTING_UPLOAD_PATH = if (downloadFolder) Const.DOWNLOAD_DIR else Const.CHRANSVER_DIR
            Const.SETTING_SHOW_HIDDEN_FILES = showHiddenFiles
            Tools.createDirectoryIfNonExist(Const.SETTING_UPLOAD_PATH)
        }


    }

}
