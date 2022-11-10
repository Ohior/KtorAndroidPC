package com.example.ktorandroidpc.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.ktorandroidpc.*
import com.example.ktorandroidpc.adapter.RecyclerAdapter
import com.example.ktorandroidpc.explorer.FileType
import com.example.ktorandroidpc.utills.*

class ExplorerFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idToolbarTextView: TextView
    private var rootDir = DataManager.getPreferenceData<StorageDataClass>(Const.FRAGMENT_DATA_KEY)!!
    private var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.id_menu_computer)?.isVisible = true
        hideAndShowMenuItem(menu)
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        menu.findItem(R.id.id_menu_computer)?.isVisible = true
        hideAndShowMenuItem(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.id_menu_computer -> {
                Navigation.findNavController(fragmentView).navigate(R.id.explorerFragment_to_connectPcFragment)
                true
            }
            R.id.id_menu_sd -> {
                rootDir = StorageDataClass(
                    rootDirectory = Tools.getExternalSDCardRootDirectory(requireActivity())!!,
                    isSdStorage = true
                )
                filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), rootDir.rootDirectory)
                changeToolbarName()
                true
            }
            R.id.id_menu_mobile -> {
                rootDir = StorageDataClass(
                    rootDirectory = Const.ROOT_PATH,
                    isSdStorage = false
                )
                filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), rootDir.rootDirectory)
                changeToolbarName()
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
        changeToolbarName()
        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
        idToolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_left, 0, 0, 0)
        idToolbarTextView.setOnClickListener {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath) {
                Navigation.findNavController(fragmentView).navigate(R.id.explorerFragment_to_connectPcFragment)
            }
        }
    }

    private fun RecyclerViewClickListener() {
        recyclerAdapter.onClickListener(object : RecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                filePath = navigateDirectoryForward(position, recyclerAdapter, requireContext(), filePath)
            }

            override fun onLongItemClick(position: Int, view: View) {
            }

            override fun onMenuClick(fileModel: FileModel, view: View) {
                view.popupMenu(fileModel, requireContext())
            }
        })
    }

    private fun Initializers() {
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
        filePath = rootDir.rootDirectory
        recyclerAdapter = RecyclerAdapter(requireContext(), idRvRootFolder, R.layout.explorer_item)
    }

    private fun changeToolbarName() {
        if (rootDir.isSdStorage) {
            idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
            idToolbarTextView.text = requireActivity().getString(R.string.format_string, "SD Storage")
        } else {
            idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
            idToolbarTextView.text = requireActivity().getString(R.string.format_string, "Local Storage")
        }

    }

    private fun hideAndShowMenuItem(menu: Menu) {
        if (rootDir.isSdStorage) {
            menu.findItem(R.id.id_menu_sd)?.isVisible = false
            menu.findItem(R.id.id_menu_mobile)?.isVisible = true
        } else {
            menu.findItem(R.id.id_menu_mobile)?.isVisible = false
            menu.findItem(R.id.id_menu_sd)?.isVisible = true
        }
    }
}
