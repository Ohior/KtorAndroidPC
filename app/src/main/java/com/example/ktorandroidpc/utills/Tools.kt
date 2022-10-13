package com.example.ktorandroidpc.utills

import android.content.Context
import android.util.Log
import android.widget.Toast

object Tools {
    fun showToast(context: Context, string: String){
        Toast.makeText(context, string,Toast.LENGTH_SHORT).show()
    }

    fun debugMessage(message: String, tag: String="DEBUG-MESSAGE"){
        Log.e(tag, message)
    }

    fun readTextFile(context: Context?, r: Int): String {
        return context!!.resources.openRawResource(r).bufferedReader()
            .use { it.readText() }
    }
}