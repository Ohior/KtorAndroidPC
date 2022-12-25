package ng.ohis.ktorandroidpc


import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import ng.ohis.ktorandroidpc.fragments.ConnectPcFragment
import ng.ohis.ktorandroidpc.fragments.ExplorerFragment
import ng.ohis.ktorandroidpc.fragments.FragmentAdapter
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.DataManager
import ng.ohis.ktorandroidpc.utills.Tools
import ng.ohis.ktorandroidpc.utills.popUpWindow

//This activity is the host for explorer fragment and connect pc fragment
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
            if (Tools.checkAllPermission(this)) {
                this.popUpWindow(
                    title = "Permission",
                    message = "Permissions 🙉 are needed for this app 📳 to run successfully"
                ) { it.setCancelable(true) }
            }
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
        DataManager.with(this)
            .setString(Const.SD_DIRECTORY_KEY, Tools.getExternalSDCardRootDirectory(this))
        // Find the view pager that will allow the user to swipe between fragments
        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        // Create an adapter that knows which fragment should be shown on each page
        val adapter =
            FragmentAdapter(supportFragmentManager, listOf(ConnectPcFragment(), ExplorerFragment()))
        // Set the adapter onto the view pager
        viewPager.adapter = adapter
    }
}
