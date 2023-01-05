package ng.ohis.ktorandroidpc.classes

import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.ShowSdCardDataclass
import ng.ohis.ktorandroidpc.adapter.StorageDataClass
import ng.ohis.ktorandroidpc.utills.Tools

interface NavbarMenuInterface {
    fun navbarMenuProvider(
        activity: FragmentActivity,
        rootDir: StorageDataClass?,
        showSdCardDataclass: ShowSdCardDataclass,
        menuItemFunc: (MenuItem) -> Boolean
    )
}

class NavbarMenuInterfaceImp : NavbarMenuInterface {
    override fun navbarMenuProvider(
        activity: FragmentActivity,
        rootDir: StorageDataClass?,
        showSdCardDataclass: ShowSdCardDataclass,
        menuItemFunc: (MenuItem) -> Boolean
    ) {
        activity.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.main_menu, menu)
                menu.findItem(R.id.id_menu_sd).isVisible = showSdCardDataclass.showSdCard
                menu.findItem(R.id.id_menu_mobile).isVisible = showSdCardDataclass.localMemory
                menu.findItem(R.id.id_menu_computer).isVisible = showSdCardDataclass.connectPc

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return menuItemFunc(menuItem)
            }

        })
    }
}