package ng.ohis.ktorandroidpc.adapter

import android.view.View

interface OnClickInterface {
    // inter face for auto-loading itemClick and longItemClick
    fun onItemClick(position: Int, view: View) {}
    fun onLongItemClick(position: Int, view: View) {}
    fun <T>onMenuClick(`object`: T, view: View){}
}