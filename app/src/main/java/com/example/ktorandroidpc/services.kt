package com.example.ktorandroidpc

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.displaySnackBar(message: String) {
    Snackbar.make(this@displaySnackBar, message, Snackbar.LENGTH_SHORT).show()
}

fun View.displaySnackBar(message: String, actionStr: String = "dismiss", function: () -> Unit) {
    Snackbar.make(this@displaySnackBar, message, Snackbar.LENGTH_LONG)
        .also { snackbar ->
            snackbar.setAction(actionStr) {
                function()
                snackbar.dismiss()
            }
        }.show()
}