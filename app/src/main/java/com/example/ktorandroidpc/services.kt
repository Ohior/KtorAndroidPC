package com.example.ktorandroidpc

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.ktorandroidpc.utills.FileModel
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

    fun View.popupMenu(fileModel: FileModel, context: Context) {
        val popupMenu = PopupMenu(context, this@popupMenu)
        popupMenu.inflate(R.menu.rv_menu_item)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.id_rv_menu_delete -> {
                    context.popUpWindow("This delete is permanent", "Delete") { adb ->

                        adb.setPositiveButton("delete") { _, _ ->
                        }
                        adb.setNegativeButton("cancel") { _, _ ->
                            adb.show().dismiss()
                        }
                    }
                    true
                }
                R.id.id_rv_menu_open -> {
                    context.openFileWithDefaultApp(fileModel.file)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
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

    fun Context.openFileWithDefaultApp(file: File) {
        val uri = FileProvider.getUriForFile(this@openFileWithDefaultApp, BuildConfig.APPLICATION_ID + ".provider", file)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        this@openFileWithDefaultApp.startActivity(intent)
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