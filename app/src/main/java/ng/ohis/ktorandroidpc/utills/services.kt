package ng.ohis.ktorandroidpc.utills

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.transition.Slide
import android.view.*
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.core.content.FileProvider
import com.google.android.material.snackbar.Snackbar
import io.ktor.server.netty.*
import ng.ohis.ktorandroidpc.BuildConfig
import ng.ohis.ktorandroidpc.R
import java.io.File


fun View.displaySnackBar(message: String) {
    Snackbar.make(this@displaySnackBar, message, Snackbar.LENGTH_SHORT).show()
}

fun View.displaySnackBar(message: String, actionStr: String = "dismiss", function: () -> Unit) {
    Snackbar.make(this@displaySnackBar, message, Snackbar.ANIMATION_MODE_SLIDE)
        .also { sb ->
            sb.setAction(actionStr) {
                function()
                sb.dismiss()
            }
        }.show()
}

fun Context.popupMenu(view: View, function: (MenuItem, pm:PopupMenu) -> Unit) {
    val popupMenu = PopupMenu(this@popupMenu, view)
    popupMenu.inflate(R.menu.rv_menu_item)
    popupMenu.setOnMenuItemClickListener { menuItem ->
        function(menuItem, popupMenu)
        true
    }
    popupMenu.show()
}

fun Context.locationPopUpWindow(
    fragmentView: View,
    layout: Int,
    gravity: Int = Gravity.BOTTOM,
    function: ((View, PopupWindow) -> Unit)? = null,
) {
    val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val view = inflater.inflate(layout, null)
    val popupWindow = PopupWindow(
        view,
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    popupWindow.elevation = 10.0F
    popupWindow.isOutsideTouchable = true
    popupWindow.isFocusable = true
    popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val slideIn = Slide()
    val slideOut = Slide()
    slideIn.slideEdge = Gravity.TOP
    slideOut.slideEdge = Gravity.END
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
        popupWindow.enterTransition = slideIn
        popupWindow.exitTransition = slideOut
    }
    if (function != null) {
        function(view, popupWindow)
    }
    popupWindow.showAtLocation(fragmentView, gravity, 0, 0)
}

fun Context.popUpWindow(
    title: String,
    layout: Int,
    lambda: ((View, AlertDialog) -> Unit)? = null
) {
    val view = LayoutInflater.from(this@popUpWindow)
        .inflate(layout, null)
    AlertDialog.Builder(this@popUpWindow).apply {
        this.setCancelable(false)
        this.setTitle(title)
        this.setView(view)
        lambda!!(view, this.show())
    }.show()
}


fun Context.popUpWindow(
    message: String,
    title: String = "",
    lambda: ((AlertDialog.Builder) -> Unit)? = null
): Boolean {
    AlertDialog.Builder(this@popUpWindow).apply {
        this.setCancelable(false)
        this.setTitle(title)
        this.setMessage(message)
        lambda!!(this)
    }.show()
    return false
}

fun Context.isThemeDark(): Boolean {
    return when (this@isThemeDark.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
        Configuration.UI_MODE_NIGHT_YES -> true
        else -> false
    }
}

fun Context.openFileWithDefaultApp(file: File): Boolean {
    return try {
        val uri =
            FileProvider.getUriForFile(this@openFileWithDefaultApp, BuildConfig.APPLICATION_ID + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        this@openFileWithDefaultApp.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    } catch (e: IllegalArgumentException) {
        this.popUpWindow(
            title = "Security",
            message = this.getString(R.string.security)
        ) {
            it.setCancelable(true)
        }
        false
    }

}

fun Activity.toggleScreenWakeLock(boolean: Boolean) {
    if (boolean) this@toggleScreenWakeLock.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    else this@toggleScreenWakeLock.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

fun Activity.menuItemClicked(nettyEngine: NettyApplicationEngine?, function: () -> Unit) {
    if (nettyEngine != null) {
        this.popUpWindow(
            title = "Notice 🔔",
            message = "PC Connection is in progress. Leaving this page 📟 will result in connection lost, which may lead to interruption of your download 👇🏾 or upload 👆🏾."
        ) { popup ->
            popup.setCancelable(true)
            popup.setPositiveButton("Continue") { _, _ ->
                function()
            }
            popup.setNegativeButton("Cancel") { _, _ ->
                popup.show().dismiss()
            }
        }
    } else function()
}
