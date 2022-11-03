package com.example.ktorandroidpc.adapter

import com.example.ktorandroidpc.utills.RecyclerAdapterDataclass
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.utills.FileModel
import kotlinx.coroutines.GlobalScope

class RecyclerAdapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    // replace all the RecyclerAdapterDataclass reference in this file with your own data class name
    private var arrayList: ArrayList<RecyclerAdapterDataclass> = ArrayList()
    private var context: Context
    private var layout: Int = 0
    private var recyclerView: RecyclerView
    private var clickListener:OnItemClickListener? = null
    private lateinit var fileModel: FileModel

    constructor(context:Context, recyclerview: RecyclerView, layout: Int, column_count:Int=1) {
        this.context = context
        this.recyclerView = recyclerview
        this.layout = layout
        this.recyclerView.layoutManager = CustomGridLayoutManager(context, column_count)
        this.recyclerView.adapter = this
    }

    constructor(context:Context,
                recyclerview: RecyclerView,
                layout:Int,
                layout_manager: RecyclerView.LayoutManager
    ){
        this.recyclerView = recyclerview
        this.context = context
        this.layout = layout
        this.recyclerView.layoutManager = layout_manager
        this.recyclerView.adapter = this
    }


    interface OnItemClickListener{
        // inter face for auto loading itemClick and longItemClick
        fun onItemClick(position: Int, view: View){}
        fun onLongItemClick(position: Int, view: View){}
    }

    fun onClickListener(listener:OnItemClickListener){
        // This function handle's click and long click.
        // Do not use this function if you are not sure what to do.
        // Use this function like this in your fragment or activity file
        //*********************************************************
        // name_of_your_recycler_view_adapter.onClickListener(
        //     object : RecyclerAdapter.OnItemClickListener {
        //         override fun onItemClick(position: Int, view: View) {
        //             TODO("Your click execution should be here")
        //         }
        //         override fun onLongItemClick(position: Int, view: View) {
        //             TODO("Your long click execution should be here")
        //         }
        //     }
        // )
        //*********************************************************************

        clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return ViewHolder(view, clickListener!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // bind your view holder class with your recycler adapter
        // i.e bind your view holder with your recyclerview
        val arraylist = this.arrayList[position]
        holder.name.text = arraylist.name
        holder.detail.text = arraylist.detail
        arraylist.drawable?.let { Glide.with(context).load(it).into(holder.image) }
    }

    override fun getItemCount(): Int {
        //get the number of item in your recyclerview
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


    class ViewHolder(
        itemView: View,
        listener:OnItemClickListener,
    ): RecyclerView.ViewHolder(itemView) {
        // initialize the item your view holder will hold
        val name: TextView = itemView.findViewById(R.id.id_tv_folder_name)
        val detail: TextView = itemView.findViewById(R.id.id_tv_folder_detail)
        val image: ImageView = itemView.findViewById(R.id.id_iv_folder_image)
        init {
            itemView.setOnLongClickListener{
                listener.onLongItemClick(adapterPosition, itemView)
                true
            }
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition, itemView)
            }
        }
    }
}
