package com.example.ktorandroidpc.fragments

import android.content.Context
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.explorer.FileType
import com.example.ktorandroidpc.openFileWithDefaultApp
import com.example.ktorandroidpc.utills.FileModel
import com.example.ktorandroidpc.utills.RecyclerAdapterDataclass
import com.example.ktorandroidpc.utills.Tools

interface ExplorerInterface {
    fun getDrawableFileType(fileType: FileType): Int {
        return when (fileType) {
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
    }

    fun loopThroughFiles(files: List<FileModel>, recyclerAdapter: RecyclerAdapter) {
        recyclerAdapter.emptyAdapter()
        recyclerAdapter.arrayList.clear()
        for (file in files) {
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = file,
                )
            )
        }
        recyclerAdapter.notifyDataSetChanged()
    }

    fun navigateDirectoryForward(position: Int?, recyclerAdapter: RecyclerAdapter, context: Context, filePath: String):String {
        var path = filePath
        if (position == null){
            val files = Tools.getFilesFromPath(path).sortedWith(compareBy { it.name })
            loopThroughFiles(files, recyclerAdapter)
            return path
        }
        val fml = recyclerAdapter.arrayList[position].fileModel
        if (fml.fileType == FileType.FOLDER) {
            path = filePath+"/${fml.name}"
            val files = Tools.getFilesFromPath(path).sortedWith(compareBy { it.name })
            loopThroughFiles(files, recyclerAdapter)
        } else {
           context.openFileWithDefaultApp(fml.file)
        }
        return path
    }

    fun navigateDirectoryBackward(recyclerAdapter: RecyclerAdapter, rootDir:String, filePath:String, function: ()->Unit): String {
        recyclerAdapter.emptyAdapter()
        recyclerAdapter.arrayList.clear()
        if (filePath.split("/").size <= rootDir.split("/").size) {
            function()
            return filePath
        }
        val directory = filePath.split("/")
            .dropLast(1)
            .joinToString("/")

        val files = Tools.getFilesFromPath(directory).sortedWith(compareBy { it.name })
        loopThroughFiles(files, recyclerAdapter)
        return directory
    }

}