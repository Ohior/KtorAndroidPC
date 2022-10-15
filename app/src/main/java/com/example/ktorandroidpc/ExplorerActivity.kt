package com.example.ktorandroidpc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.utills.*
import com.google.gson.Gson
import kotlinx.coroutines.*

class ExplorerActivity : AppCompatActivity() {
    private lateinit var idRvFolderItems: RecyclerView
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

    override fun onBackPressed() {
        coroutineScope.cancel()
        super.onBackPressed()
    }

    override fun onDestroy() {
        if (coroutineScope.isActive){
            coroutineScope.cancel()
        }
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explorer)

        Initializers()

        FillRecyclerView()

        RecyclerViewClicklistener()
    }

    private fun RecyclerViewClicklistener() {
        recyclerAdapter.onClickListener(object : RecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
            }

            override fun onLongItemClick(position: Int, view: View) {
            }
        })
    }

    private fun FillRecyclerView() {
        coroutineScope.launch {
            val modelList =
                DataManager.retrievePreferenceData(Const.ROOT_FOLDER_KEY)
            for (i in modelList) {
                recyclerAdapter.addToAdapter(RecyclerAdapterDataclass(name = i.name, detail = i.path))
            }
        }
    }

    private fun Initializers() {
        idRvRootFolder = findViewById(R.id.id_rv_root_folder)
//        idRvFolderItems = findViewById(R.id.id_rv_folder_items)
        recyclerAdapter = RecyclerAdapter(applicationContext, idRvRootFolder, R.layout.explorer_item)
    }
}