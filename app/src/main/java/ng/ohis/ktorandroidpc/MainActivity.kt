package ng.ohis.ktorandroidpc


import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import ng.ohis.ktorandroidpc.fragments.ExplorerFragment
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.DataManager
import ng.ohis.ktorandroidpc.utills.Tools
import java.lang.reflect.Method

//This activity is the host for explorer fragment and connect pc fragment
class MainActivity : AppCompatActivity() {

    override fun onDestroy() {
        baseContext.cacheDir.deleteRecursively()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        display custom actionbar
        setSupportActionBar(findViewById(R.id.id_toolbar))
        SettingsActivity.appSettings(this)
        DataManager.with(this).setString(Const.SD_DIRECTORY_KEY, Tools.getExternalSDCardRootDirectory(this))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
//        make sure menu is displaying icons
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
//        this holds general menu item
        return when (item.itemId) {
            R.id.id_menu_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
