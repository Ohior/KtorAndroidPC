package com.example.ktorandroidpc.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.explorer.FileType
import com.example.ktorandroidpc.explorer.FileUtils
import com.example.ktorandroidpc.utills.FileModel
import com.example.ktorandroidpc.utills.RecyclerAdapterDataclass
import com.example.ktorandroidpc.utills.Tools

class ExplorerFragment : Fragment() {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var fileModelList: ArrayList<FileModel>
    private lateinit var idToolbarTextView: TextView
    private val mDirectory by lazy { Environment.getExternalStorageDirectory().absolutePath }
    private var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
        idToolbarTextView.text = requireActivity().getString(R.string.format_string, "Local Storage")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.id_menu_computer)?.isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.id_menu_computer -> {
                Navigation.findNavController(fragmentView).navigate(R.id.explorerFragment_to_connectPcFragment)
                true
            }
            R.id.id_menu_sd -> {
                Navigation.findNavController(fragmentView).navigate(R.id.explorerFragment_to_sdExplorerFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_explorer, container, false)


        Initializers()

        FragmentExecutable()

        RecyclerViewClickListener()

        return fragmentView
    }

    private fun FragmentExecutable() {
        idToolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_left, 0, 0, 0)
        idToolbarTextView.setOnClickListener {
            NavigateDirectoryBackward()
        }
    }

    private fun NavigateDirectoryBackward() {
        recyclerAdapter.emptyAdapter()
        fileModelList.clear()
        val directory = filePath.split("/")
        val dir = directory.subList(0, directory.size - 1)
        val path = dir.joinToString("/")
        val files = Tools.getFilesFromPath(mDirectory + path).sortedWith(compareBy { it.name })
        filePath = path
        LoopThroughFiles(files)
        if (dir.isEmpty()) {
            Navigation.findNavController(fragmentView).navigate(R.id.explorerFragment_to_connectPcFragment)
        }
    }

    private fun RecyclerViewClickListener() {
        fileModelList = Tools.getRootFolder() as ArrayList<FileModel>
        fileModelList.sortWith(compareBy { it.name })
        LoopThroughFiles(fileModelList.toList())
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
            val files = Tools.getFilesFromPath(mDirectory + filePath)//.sortedWith(compareBy { it.name })
            LoopThroughFiles(files)
        } else {
            val uri = Uri.fromFile(fml.file)
            val dat = uri.toString().replaceFirst("file", "content")
            val intent =  Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(dat.toUri(), "*/*");
            startActivity(intent);
        }
    }

    private fun LoopThroughFiles(files: List<FileModel>) {
        for (file in files) {
            recyclerAdapter.addToAdapter(
                RecyclerAdapterDataclass(
                    fileModel = file,
                )
            )
            fileModelList.add(file)
        }
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun Initializers() {
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
//        idRvFolderItems = findViewById(R.id.id_rv_folder_items)
        recyclerAdapter = RecyclerAdapter(requireContext(), idRvRootFolder, R.layout.explorer_item)
    }

    private fun getDrawableFileType(fileType: FileType): Int {
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
}