package ng.ohis.ktorandroidpc.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
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
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import ng.ohis.ktorandroidpc.BuildConfig
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.NavigateRecyclerAdapter
import ng.ohis.ktorandroidpc.adapter.OnClickInterface
import ng.ohis.ktorandroidpc.adapter.RecyclerAdapter
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.openFileWithDefaultApp
import ng.ohis.ktorandroidpc.popUpWindow
import ng.ohis.ktorandroidpc.utills.*
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.DataManager
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.File

class ExplorerFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idNavigateRecyclerView: RecyclerView
    private lateinit var navigateRecyclerAdapter: NavigateRecyclerAdapter
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
                Tools.navigateFragmentToFragment(fragmentView, R.id.explorerFragment_to_connectPcFragment)
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
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_explorer, container, false)

        variableInitializers()

        fragmentExecutable()

        recyclerViewClickListener()

        return fragmentView
    }

    override fun navigateDirectoryForward(
        position: Int?,
        recyclerAdapter: RecyclerAdapter,
        context: Context,
        path: String
    ): String {
        filePath = super.navigateDirectoryForward(position, recyclerAdapter, context, path)
        folderNavigateRecyclerView()
        return filePath
    }

    override fun navigateDirectoryBackward(
        recyclerAdapter: RecyclerAdapter,
        rootDir: String,
        path: String
    ): String {
        filePath = super.navigateDirectoryBackward(recyclerAdapter, rootDir, path)
        folderNavigateRecyclerView()
        return filePath
    }

    private fun fragmentExecutable() {
        changeToolbarName()
        idToolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_left, 0, 0, 0)
        idToolbarTextView.setOnClickListener {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                Tools.navigateFragmentToFragment(fragmentView, R.id.explorerFragment_to_connectPcFragment)
            }
        }
    }

    private fun recyclerViewClickListener() {
        folderNavigateRecyclerView()
        recyclerAdapter.onClickListener(object : RecyclerAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, view: View) {
                filePath = navigateDirectoryForward(position, recyclerAdapter, requireContext(), filePath)
            }
            override fun onMenuClick(fileModel: FileModel, view: View) {
                PopupMenu(context, view).apply {
                    this.inflate(R.menu.rv_menu_item)
                    this.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.id_rv_menu_delete -> {
                                Tools.popUpWindow(requireContext(),"This delete is permanent","Delete File") { builder: AlertDialog.Builder ->
                                    builder.setPositiveButton("Delete") { _, _ ->
                                        deleteFileUri = deleteFileFromStorage(fileModel.file)
                                        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
                                    }
                                    builder.setNegativeButton("Cancel") { _, _ ->
                                        builder.show().dismiss()
                                    }
                                }
                                true
                            }
                            R.id.id_rv_menu_open -> {
                                if (fileModel.fileType == FileType.FOLDER || !requireContext().openFileWithDefaultApp(fileModel.file)) {
                                    Tools.showToast(requireContext(), "No App To open this File! 😢")
                                } else filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
                                true
                            }
                            else -> false
                        }
                    }
                    this.show()
                }
            }
        })
    }

    private fun variableInitializers() {
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
        filePath = rootDir.rootDirectory
        recyclerAdapter = RecyclerAdapter(requireContext(), idRvRootFolder, R.layout.explorer_item)
        navigateRecyclerAdapter = NavigateRecyclerAdapter(requireContext(), idNavigateRecyclerView)
        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
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

    private fun deleteFileFromStorage(file: File): Uri {
        if (rootDir.isSdStorage) {
            requireContext().popUpWindow(
                title = "Security",
                message = requireActivity().getString(R.string.security)
            ) {
                it.setCancelable(true)
            }
            return file.toUri()
        }
        val uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
        try {
            File(file.absolutePath).deleteRecursively()
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

    private fun openDocumentTree() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivity(intent)
    }

    private fun registerDeleteResult() {
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

    private fun folderNavigateRecyclerView() {
        navigateRecyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath = filePath.split("/")
                    .dropLastWhile { it != navigateRecyclerAdapter.getItemAt(position).name }
                    .joinToString("/")
                filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
            }
        })
        updateNavigationBarFolderRecyclerView(filePath, rootDir, navigateRecyclerAdapter)
    }
}
