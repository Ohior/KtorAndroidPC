package com.example.ktorandroidpc

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.displaySnackbar(message: String) {
    Snackbar.make(this@displaySnackbar, message, Snackbar.LENGTH_SHORT).show()
}

fun View.displaySnackbar(message: String, actionStr: String = "dismiss", function: () -> Unit) {
    Snackbar.make(this@displaySnackbar, message, Snackbar.LENGTH_SHORT)
        .also { snackbar ->
            snackbar.setAction(actionStr) {
                function()
                snackbar.dismiss()
            }
        }.show()
}