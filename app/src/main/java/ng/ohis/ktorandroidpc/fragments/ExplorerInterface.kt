package ng.ohis.ktorandroidpc.fragments

import android.content.Context
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.RecyclerAdapter
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.openFileWithDefaultApp
import ng.ohis.ktorandroidpc.utills.FileModel
import ng.ohis.ktorandroidpc.utills.RecyclerAdapterDataclass
import ng.ohis.ktorandroidpc.utills.Tools

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