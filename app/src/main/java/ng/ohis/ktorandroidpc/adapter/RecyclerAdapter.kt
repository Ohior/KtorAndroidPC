package ng.ohis.ktorandroidpc.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.popUpWindow

class RecyclerAdapter(
    private val mContext: Context,
    recyclerview: RecyclerView,
    private val layout: Int,
    column_count: Int = 1
) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
    // replace all the RecyclerAdapterDataclass reference in this file with your own data class name
    private var recyclerArrayList: ArrayList<RecyclerAdapterDataclass> = ArrayList()
    private var clickListener: OnClickInterface? = null

    init {
        recyclerview.layoutManager = CustomGridLayoutManager(mContext, column_count)
        recyclerview.adapter = this
    }


    fun onClickListener(listener: OnClickInterface) {
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
        val view = LayoutInflater.from(mContext).inflate(layout, parent, false)
        return ViewHolder(view, clickListener!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // bind your view holder class with your recycler adapter
        // i.e bind your view holder with your recyclerview
        val array = this.recyclerArrayList[position]
        holder.name.text = array.fileModel.name
        holder.detail.text = array.fileModel.path
        val bitmap = getBitmapFromMediaStore(array.fileModel)
        if (bitmap != null) {
            Glide.with(mContext).load(bitmap).into(holder.image)
        } else {
            Glide.with(mContext).load(array.fileModel.drawable).into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        //get the number of item in your recyclerview
        return recyclerArrayList.size
    }

    fun emptyAdapter() {
        //remove all item from your recyclerview
        recyclerArrayList.clear()
        notifyDataSetChanged()
    }

    fun addToAdapter(element: RecyclerAdapterDataclass) {
        // add item to your recyclerview
        recyclerArrayList.add(element)
    }

    fun addToAdapter(index: Int, element: RecyclerAdapterDataclass) {
        // add item to an index spot of your recyclerview
        recyclerArrayList.add(index, element)
    }

    fun removeAt(element: RecyclerAdapterDataclass){
        recyclerArrayList.remove(element)
    }

    fun getItemAt(position: Int): RecyclerAdapterDataclass {
        return recyclerArrayList[position]
    }

    private fun getBitmapFromMediaStore(fileModel: FileModel): Uri? {
        return when (fileModel.fileType) {
            FileType.VIDEO -> {
                Uri.fromFile(fileModel.file)
            }
            FileType.IMAGE -> {
                Uri.fromFile(fileModel.file)
            }
            else -> {
                null
            }
        }
    }


    inner class ViewHolder(
        itemView: View,
        listener: OnClickInterface,
    ) : RecyclerView.ViewHolder(itemView) {
        // initialize the item your view holder will hold
        val name: TextView = itemView.findViewById(R.id.id_tv_folder_name)
        val detail: TextView = itemView.findViewById(R.id.id_tv_folder_detail)
        val image: ImageView = itemView.findViewById(R.id.id_iv_folder_image)
        private val menu: TextView = itemView.findViewById(R.id.id_tv_menu_item)

        init {
            itemView.setOnLongClickListener {
                listener.onLongItemClick(adapterPosition, itemView)
                true
            }
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition, itemView)
            }
            menu.setOnClickListener {
                listener.onMenuClick(recyclerArrayList[adapterPosition].fileModel, it, adapterPosition)
            }
        }
    }
}
