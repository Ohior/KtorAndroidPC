package com.example.ktorandroidpc.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.utills.RecyclerAdapterDataclass

class DownloadAdapter(val activity: Activity, recyclerView: RecyclerView):RecyclerView.Adapter<DownloadAdapter.ViewHolder>() {
    private var arrayList: ArrayList<RecyclerAdapterDataclass> = ArrayList()
    init {
        recyclerView.layoutManager = CustomGridLayoutManager(activity, 1)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(activity).inflate(R.layout.download_progress_bar, parent, false)
        return DownloadAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val array = this.arrayList[position]
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    fun emptyAdapter(){
        //remove all item from your recyclerview
        arrayList.clear()
    }

    fun addToAdapter(element: RecyclerAdapterDataclass){
        // add item to your recyclerview
        arrayList.add(element)
    }

    fun addToAdapter(index:Int, element:RecyclerAdapterDataclass){
        // add item to an index spot of your recyclerview
        arrayList.add(index, element)
    }


    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){}
}