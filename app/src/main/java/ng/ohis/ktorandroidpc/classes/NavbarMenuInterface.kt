package ng.ohis.ktorandroidpc.classes

import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.StorageDataClass
import ng.ohis.ktorandroidpc.utills.Tools

interface NavbarMenuInterface {
    fun navbarMenuProvider(
        activity: FragmentActivity,
        rootDir: StorageDataClass?,
        showComputerIcon: Boolean,
        showDeviceIcon: Boolean = true,
        menuItemFunc: (MenuItem) -> Boolean
    )
}

class NavbarMenuInterfaceImp : NavbarMenuInterface {
    override fun navbarMenuProvider(
        activity: FragmentActivity,
        rootDir: StorageDataClass?,
        showComputerIcon: Boolean,
        showDeviceIcon: Boolean,
        menuItemFunc: (MenuItem) -> Boolean
    ) {
        activity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.main_menu, menu)
                menu.findItem(R.id.id_menu_computer)?.isVisible = showComputerIcon
                menu.findItem(R.id.id_menu_connect_device)?.isVisible = showDeviceIcon
                if (rootDir == null) {
                    menu.findItem(R.id.id_menu_sd)?.isVisible = false
                    menu.findItem(R.id.id_menu_mobile)?.isVisible = false
                } else {
                    menu.findItem(R.id.id_menu_sd)?.isVisible =
                        if (!showComputerIcon) Tools.isExternalStorageAvailable() else !rootDir.isSdStorage
                    menu.findItem(R.id.id_menu_mobile)?.isVisible = rootDir.isSdStorage
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return menuItemFunc(menuItem)
            }

        })
    }
}