package ng.ohis.ktorandroidpc.adapter

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ng.ohis.ktorandroidpc.utills.Tools

class CustomGridLayoutManager(val context: Context, column_count: Int = 1, orientation:Int = RecyclerView.VERTICAL)
    : GridLayoutManager(context, column_count, orientation, false) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Tools.debugMessage("Inconsistency detected")
        }
    }
}