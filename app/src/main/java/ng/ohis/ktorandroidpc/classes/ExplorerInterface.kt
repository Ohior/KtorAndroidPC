package ng.ohis.ktorandroidpc.classes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.View
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.*
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
        for ((index, file) in files.withIndex()) {
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = file,
                )
            )
            recyclerAdapter.notifyItemInserted(index)
        }
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
            val files = Tools.getFilesFromPath(path).sortedWith(compareBy { it.name.lowercase() })
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

            val files = Tools.getFilesFromPath(directory).sortedWith(compareBy { it.name.lowercase() })
            loopThroughFiles(files, recyclerAdapter)
            directory
        }
    }

    fun updateNavigationBarFolderRecyclerView(
        filePath: String,
        rootDir: StorageDataClass,
        navigateRecyclerAdapter: NavbarRecyclerAdapter
    ) {
        val path = filePath.split("/").drop((rootDir.rootDirectory.split("/").size) - 1)
        navigateRecyclerAdapter.clearAdapter()
        for ((i, p) in path.withIndex()) {
            navigateRecyclerAdapter.addToAdapter(NavigateRecyclerAdapterDataclass(name = p))
            navigateRecyclerAdapter.notifyItemChanged(i)
        }
    }


    /// Config
    private fun openDocumentTree(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        activity.startActivity(intent)
    }

    fun getToolbarName(rootDir: StorageDataClass?, activity: Activity, title:String?=null): String {
        return if (rootDir == null){
            activity.getString(R.string.format_string, title)
        }
        else if (rootDir.isSdStorage) {
            activity.getString(R.string.format_string, "SD Storage")
        } else{
            activity.getString(R.string.format_string, "Local Storage")
        }

    }

    private fun navbarRecyclerView(
        navbarRecyclerAdapter: NavbarRecyclerAdapter,
        filePath: String,
        rootDir: StorageDataClass,
        recyclerAdapter: RecyclerAdapter,
        context: Context
    ) {
        var fp: String
        navbarRecyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                fp = filePath.split("/")
                    .dropLastWhile { it != navbarRecyclerAdapter.getItemAt(position).name }
                    .joinToString("/")
                fp = navigateDirectoryForward(null, recyclerAdapter, context, fp)
                updateNavigationBarFolderRecyclerView(fp, rootDir, navbarRecyclerAdapter)
            }
        })
    }

}