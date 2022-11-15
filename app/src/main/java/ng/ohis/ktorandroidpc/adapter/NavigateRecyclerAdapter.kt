package ng.ohis.ktorandroidpc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.utills.NavigateRecyclerAdapterDataclass

class NavigateRecyclerAdapter(
    private val mContext: Context,
    recyclerView: RecyclerView
) : RecyclerView.Adapter<NavigateRecyclerAdapter.ViewHolder>() {
    private var navigateRecyclerArrayList = ArrayList<NavigateRecyclerAdapterDataclass>()
    private var clickListener: OnClickInterface? = null

    init {
        recyclerView.layoutManager = CustomGridLayoutManager(context =  mContext, orientation = RecyclerView.HORIZONTAL)
        recyclerView.adapter = this
    }

    fun onClickListener(listener: OnClickInterface) {
        clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.navigate_rv_item, parent, false)
        return ViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val array = this.navigateRecyclerArrayList[position]
        holder.name.text = array.name
    }

    fun clearAdapter() {
        navigateRecyclerArrayList.clear()
    }

    fun addToAdapter(element: NavigateRecyclerAdapterDataclass) {
        navigateRecyclerArrayList.add(element)
    }

    override fun getItemCount(): Int {
        return navigateRecyclerArrayList.size
    }

    fun getItemAt(position: Int):NavigateRecyclerAdapterDataclass{
        return navigateRecyclerArrayList[position]
    }

    inner class ViewHolder(
        itemView: View,
        clickListener: OnClickInterface?,
    ) : RecyclerView.ViewHolder(itemView) {
        val name:TextView = itemView.findViewById(R.id.id_tv_navigate_folder)
        init {
            itemView.setOnClickListener {
                clickListener?.onItemClick(adapterPosition, itemView)
            }
        }
    }
}
