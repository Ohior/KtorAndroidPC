package ng.ohis.ktorandroidpc.adapter

import android.view.View

interface OnClickInterface {
    // inter face for auto-loading itemClick and longItemClick
    fun onItemClick(position: Int, view: View) {}
    fun onLongItemClick(position: Int, view: View) {}
    fun onMenuClick(fileModel: FileModel, view: View, position: Int = 0){}
}