package ng.ohis.ktorandroidpc

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.inflate
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
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

fun Context.popupMenu(view: View, function: (MenuItem) -> Unit) {
    val popupMenu = PopupMenu(this@popupMenu, view)
    popupMenu.inflate(R.menu.rv_menu_item)
    popupMenu.setOnMenuItemClickListener { menuItem ->
        function(menuItem)
        true
    }
    popupMenu.show()
}

fun Context.popUpWindow(
    fragmentView: View,
    layout: Int,
    lambda: ((View,PopupWindow) -> Unit)? = null
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
    val slidein = Slide()
    val slideout = Slide()
    slidein.slideEdge = Gravity.TOP
    slideout.slideEdge = Gravity.END
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
        popupWindow.enterTransition = slidein
        popupWindow.exitTransition = slideout
    }
    if (lambda != null) {
        lambda(view, popupWindow)
    }
    popupWindow.showAtLocation(fragmentView, Gravity.BOTTOM, 0, 0)
}

fun Context.popUpWindow(
    message: String,
    title: String = "",
    lambda: ((AlertDialog.Builder) -> Unit)? = null
) {
    AlertDialog.Builder(this@popUpWindow).apply {
        this.setCancelable(false)
        this.setTitle(title)
        this.setMessage(message)
        lambda!!(this)
    }.show()
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


/**
 * Rename file.
 *
 * @param uri    - filepath
 * @param rename - the name you want to replace with original.
 */
private fun Context.rename(uri: Uri, rename: String) {
    //create content values with new name and update
    val contentValues = ContentValues()
    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, rename)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this@rename.contentResolver.update(uri, contentValues, null)
    }
}
