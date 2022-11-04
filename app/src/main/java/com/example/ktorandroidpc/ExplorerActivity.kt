package com.example.ktorandroidpc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.explorer.FileType
import com.example.ktorandroidpc.explorer.FileUtils
import com.example.ktorandroidpc.utills.*


class ExplorerActivity : AppCompatActivity() {
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var fileModelList: ArrayList<FileModel>
    private val mDirectory by lazy { Environment.getExternalStorageDirectory().absolutePath }
    private var filePath = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explorer)

        Initializers()

        FillRecyclerView()

        RecyclerViewClickListener()
    }

    override fun onBackPressed() {
        NavigateDirectoryBackward()
    }

    private fun NavigateDirectoryBackward() {
        recyclerAdapter.emptyAdapter()
        fileModelList.clear()
        val directory = filePath.split("/")
        val dir = directory.subList(0, directory.size - 1)
        val path = dir.joinToString("/")
        val files = StoreDirectoryFolder(mDirectory + path).sortedWith(compareBy { it.name })
        filePath = path
        LoopThroughFiles(files)
        if (dir.isEmpty()) {
            super.onBackPressed()
        }
    }

    private fun RecyclerViewClickListener() {
        recyclerAdapter.onClickListener(object : RecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                NavigateDirectoryForward(position)
            }

            override fun onLongItemClick(position: Int, view: View) {
            }
        })
    }

    private fun NavigateDirectoryForward(position: Int) {
        val fml = fileModelList[position]
        if (fml.fileType == FileType.FOLDER) {
            recyclerAdapter.emptyAdapter()
            fileModelList.clear()
            filePath += "/${fml.name}"
            val files = StoreDirectoryFolder(mDirectory + filePath).sortedWith(compareBy { it.name })
            LoopThroughFiles(files)
        }
    }

    private fun LoopThroughFiles(files: List<FileModel>) {
        for (file in files) {
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = file,
                    drawable = when (file.fileType) {
                        FileType.FOLDER -> {
                            R.drawable.folder
                        }
                        FileType.AUDIO -> {
                            R.drawable.audio
                        }
                        FileType.IMAGE -> {
                            R.drawable.image
                        }
                        FileType.VIDEO -> {
                            R.drawable.video
                        }
                        else -> {
                            R.drawable.file
                        }
                    }
                )
            )
            fileModelList.add(file)
        }
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun FillRecyclerView() {
        fileModelList = Tools.getRootFolder() as ArrayList<FileModel>
//        coroutineScope.launch {
        fileModelList.sortWith(compareBy { it.name })
        for (file in fileModelList) {
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = file,
                    drawable = when (file.fileType) {
                        FileType.FOLDER -> {
                            R.drawable.folder
                        }
                        FileType.AUDIO -> {
                            R.drawable.audio
                        }
                        FileType.IMAGE -> {
                            R.drawable.image
                        }
                        FileType.VIDEO -> {
                            R.drawable.video
                        }
                        else -> {
                            R.drawable.file
                        }
                    }
                )
            )
        }
//            }
    }

    private fun Initializers() {
        idRvRootFolder = findViewById(R.id.id_rv_root_folder)
//        idRvFolderItems = findViewById(R.id.id_rv_folder_items)
        recyclerAdapter = RecyclerAdapter(applicationContext, idRvRootFolder, R.layout.explorer_item)
    }

    private fun StoreDirectoryFolder(path: String): List<FileModel> {
        return FileUtils.getFileModelsFromFiles(
            FileUtils.getFilesFromPath(
                path,
                showHiddenFiles = true
            )
        )
    }

    private fun DisplayFiles() {

    }
}