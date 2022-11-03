package com.example.ktorandroidpc

import android.view.View
import com.google.android.material.snackbar.Snackbar

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