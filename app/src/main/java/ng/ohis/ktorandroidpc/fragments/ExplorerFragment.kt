package ng.ohis.ktorandroidpc.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import ng.ohis.ktorandroidpc.BuildConfig
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.RecyclerAdapter
import ng.ohis.ktorandroidpc.openFileWithDefaultApp
import ng.ohis.ktorandroidpc.utills.*
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.DataManager
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.File

class ExplorerFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idToolbarTextView: TextView
    private var rootDir = DataManager.getPreferenceData<StorageDataClass>(Const.FRAGMENT_DATA_KEY)!!
    private var filePath = ""
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var deleteFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        registerDeleteResult()
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
//                view.popupMenu(fileModel, requireContext())
                val popupMenu = PopupMenu(context, view)
                popupMenu.inflate(R.menu.rv_menu_item)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.id_rv_menu_delete -> {
                            Tools.popUpWindow(
                                requireContext(),
                                "This delete is permanent",
                                "Delete File") {builder: AlertDialog.Builder ->
                                builder.setPositiveButton("Delete"){_,_->
                                    deleteFileUri = deleteFileFromStorage(fileModel.file)
                                    navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
                                }
                                builder.setNegativeButton("Cancel"){d,_->
                                    builder.show().dismiss()
                                }
                            }
                            true
                        }
                        R.id.id_rv_menu_open -> {
                            if (requireContext().openFileWithDefaultApp(fileModel.file)) {
                                Tools.showToast(requireContext(), "No App To open this File! 😢")
                            }
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
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

    fun deleteFileFromStorage(file: File): Uri {
//        val uri = Uri.fromFile(file)
        val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
        try {
            File(file.absolutePath).deleteRecursively()
//            context?.contentResolver?.delete(uri, null, null)
        } catch (e: SecurityException) {
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(context?.contentResolver!!, listOf(uri)).intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val recoverableSecurityException = e as RecoverableSecurityException
                    recoverableSecurityException.userAction.actionIntent.intentSender
                }
                else -> null
            }
            intentSender?.let { sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
            }
        }
        return uri
    }

    fun registerDeleteResult() {
        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && deleteFileUri != null) {
                    deleteFileFromStorage(deleteFileUri!!.toFile())
                }
                Tools.showToast(requireContext(), "File deleted Successfully")
            } else {
                Tools.showToast(requireContext(), "File delete Aborted")
            }
        }
    }
}
