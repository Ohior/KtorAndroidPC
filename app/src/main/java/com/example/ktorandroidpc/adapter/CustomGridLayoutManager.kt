package com.example.ktorandroidpc.adapter

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktorandroidpc.utills.Tools

class CustomGridLayoutManager(val context: Context, column_count: Int)
    : GridLayoutManager(context, column_count, VERTICAL, false) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Tools.debugMessage("Inconsistency detected")
        }
    }
}