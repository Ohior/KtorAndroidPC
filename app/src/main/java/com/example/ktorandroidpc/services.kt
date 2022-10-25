package com.example.ktorandroidpc

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun View.displaySnakbar(message: String) {
    Snackbar.make(this@displaySnakbar, message, Snackbar.LENGTH_SHORT).show()
}

fun View.displaySnakbar(message: String, actionStr: String = "dismiss", function: () -> Unit) {
    Snackbar.make(this@displaySnakbar, message, Snackbar.LENGTH_SHORT)
        .also { snackbar ->
            snackbar.setAction(actionStr) {
                function()
                snackbar.dismiss()
            }
        }.show()
}