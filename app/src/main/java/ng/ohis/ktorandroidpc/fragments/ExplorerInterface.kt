package ng.ohis.ktorandroidpc.fragments

import android.content.Context
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.NavigateRecyclerAdapter
import ng.ohis.ktorandroidpc.adapter.RecyclerAdapter
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.openFileWithDefaultApp
import ng.ohis.ktorandroidpc.utills.*

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
        // remove all data from recycler adapter amd fill it with updated list of file-model
        recyclerAdapter.emptyAdapter()
        for (file in files) {
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = file,
                )
            )
        }
        recyclerAdapter.notifyDataSetChanged()
    }

    fun navigateDirectoryForward(
        position: Int?,
        recyclerAdapter: RecyclerAdapter,
        context: Context,
        filePath: String
    ): String {
        // enter into new folder
        var path = filePath
        if (position == null) {
            // because position is null return the passed file path, this usually returns the root path
            val files = Tools.getFilesFromPath(path).sortedWith(compareBy { it.name })
            loopThroughFiles(files, recyclerAdapter)
            return path
        }
        // get the folder at position
        val fml = recyclerAdapter.getItemAt(position).fileModel
        if (fml.fileType == FileType.FOLDER) {
            // enter the file because it is a folder
            path = filePath + "/${fml.name}"
            val files = Tools.getFilesFromPath(path).sortedWith(compareBy { it.name })
            loopThroughFiles(files, recyclerAdapter)
        } else {
            // open the file because it is not a folder
            context.openFileWithDefaultApp(fml.file)
        }
        return path
    }

    fun navigateDirectoryBackward(
        recyclerAdapter: RecyclerAdapter,
        rootDir: String,
        filePath: String,
    ): String {
        // go backward in directory
        recyclerAdapter.emptyAdapter()
        return if (filePath.split("/").size <= rootDir.split("/").size) {
            // if user is in root directory,
            String()
        } else {
            // user is not in root directory. spit the path, drop the last item and join it back to a string
            val directory = filePath.split("/")
                .dropLast(1)
                .joinToString("/")

            val files = Tools.getFilesFromPath(directory).sortedWith(compareBy { it.name })
            loopThroughFiles(files, recyclerAdapter)
            directory
        }
    }

    fun updateNavigationBarFolderRecyclerView(filePath: String, rootDir: StorageDataClass, navigateRecyclerAdapter: NavigateRecyclerAdapter) {
        val path = filePath.split("/").drop((rootDir.rootDirectory.split("/").size) - 1)
        navigateRecyclerAdapter.clearAdapter()
        for ((i, p) in path.withIndex()) {
            navigateRecyclerAdapter.addToAdapter(NavigateRecyclerAdapterDataclass(name = p))
            navigateRecyclerAdapter.notifyItemChanged(i)
        }
    }

}