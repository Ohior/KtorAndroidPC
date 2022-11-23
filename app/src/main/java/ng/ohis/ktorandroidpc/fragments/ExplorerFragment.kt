package ng.ohis.ktorandroidpc.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ng.ohis.ktorandroidpc.BuildConfig
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.NavbarRecyclerAdapter
import ng.ohis.ktorandroidpc.adapter.OnClickInterface
import ng.ohis.ktorandroidpc.adapter.RecyclerAdapter
import ng.ohis.ktorandroidpc.classes.ExplorerInterface
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.openFileWithDefaultApp
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.FileModel
import ng.ohis.ktorandroidpc.utills.StorageDataClass
import ng.ohis.ktorandroidpc.utills.Tools
import java.io.File

class ExplorerFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idNavigateRecyclerView: RecyclerView
    private lateinit var navbarRecyclerAdapter: NavbarRecyclerAdapter
    private lateinit var idToolbarTextView: TextView
    private lateinit var rootDir: StorageDataClass
    private var filePath = ""
    private var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    deleteFileUri?.let { it1 -> deleteFileFromStorage(it1.toFile()) }
                }
                Tools.showToast(requireActivity(), "File(s) deleted")
            } else {
                Tools.showToast(requireActivity(), "File(s) not deleted")
            }
        }
    private var deleteFileUri: Uri? = null

    private var sdCardDeleteResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            File(deleteFileUri.toString()).canonicalFile.delete()
            Tools.debugMessage(deleteFileUri.toString(), "IT")
            Tools.showToast(requireActivity(), "File(s) deleted")
            requireActivity().contentResolver.delete(deleteFileUri!!, null, null)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        registerDeleteResult()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.id_menu_computer)?.isVisible = true
        hideAndShowMenuItem(menu, rootDir)
    }

    override fun onOptionsMenuClosed(menu: Menu) {
        super.onOptionsMenuClosed(menu)
        menu.findItem(R.id.id_menu_computer)?.isVisible = true
        hideAndShowMenuItem(menu, rootDir)
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
                idToolbarTextView.text = getToolbarName(rootDir, requireActivity())
                true
            }
            R.id.id_menu_mobile -> {
                rootDir = StorageDataClass(
                    rootDirectory = Const.ROOT_PATH,
                    isSdStorage = false
                )
                filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), rootDir.rootDirectory)
                idToolbarTextView.text = getToolbarName(rootDir, requireActivity())
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
        filePath: String
    ): String {
        this.filePath = super.navigateDirectoryForward(position, recyclerAdapter, context, filePath)
        navbarRecyclerView()
        return this.filePath
    }

    override fun navigateDirectoryBackward(
        recyclerAdapter: RecyclerAdapter,
        rootDir: String,
        filePath: String
    ): String {
        this.filePath = super.navigateDirectoryBackward(recyclerAdapter, rootDir, filePath)
        navbarRecyclerView()
        return this.filePath
    }

    private fun fragmentExecutable() {
        idToolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_arrow_left, 0, 0, 0)
        idToolbarTextView.setOnClickListener {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                requireActivity().onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }

    private fun recyclerViewClickListener() {
//        navbarRecyclerView()
        recyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath = navigateDirectoryForward(position, recyclerAdapter, requireContext(), filePath)
            }


            override fun onMenuClick(fileModel: FileModel, view: View, position: Int) {
                PopupMenu(context, view).apply {
                    this.inflate(R.menu.rv_menu_item)
                    this.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.id_rv_menu_delete -> {
                                Tools.popUpWindow(
                                    requireContext(),
                                    "This delete is permanent",
                                    "Delete File"
                                ) { builder: AlertDialog.Builder ->
                                    builder.setPositiveButton("Delete") { _, _ ->
                                        deleteFileUri = deleteFileFromStorage(fileModel.file)
                                        filePath =
                                            navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
                                    }
                                    builder.setNegativeButton("Cancel") { _, _ ->
                                        builder.show().dismiss()
                                    }
                                }
                                true
                            }
                            R.id.id_rv_menu_open -> {
                                if (fileModel.fileType == FileType.FOLDER || !requireContext().openFileWithDefaultApp(
                                        fileModel.file
                                    )
                                ) {
                                    Tools.showToast(requireContext(), "No App To open this File! 😢")
                                } else filePath =
                                    navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
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
        rootDir = Gson().fromJson(requireArguments().getString(Const.FRAGMENT_DATA_KEY), StorageDataClass::class.java)
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
        idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
        filePath = rootDir.rootDirectory
        recyclerAdapter = RecyclerAdapter(requireContext(), idRvRootFolder, R.layout.explorer_item)
        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)
        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
        idToolbarTextView.text = getToolbarName(rootDir, requireActivity())
    }

    private fun deleteFileFromStorage(file: File): Uri {
        val fileUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", file)
        if (rootDir.isSdStorage) {
//            sdCardDeleteResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
            DocumentFile.fromSingleUri(requireContext(), fileUri)?.delete()
        } else {
            try {
                File(file.absolutePath).deleteRecursively()
//                            requireActivity().contentResolver.delete(fileUri, null, null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(requireActivity().contentResolver, listOf(fileUri)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(IntentSenderRequest.Builder(sender).build())
                }
            }
        }
//        if (rootDir.isSdStorage) {
//            requireContext().popUpWindow(
//                title = "Security",
//                message = requireActivity().getString(R.string.security)
//            ) {
//                it.setCancelable(true)
//            }
//            return file.toUri()
//        }
//        try {
//            File(file.absolutePath).deleteRecursively()
//        } catch (e: SecurityException) {
//            val intentSender = when {
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
//                    MediaStore.createDeleteRequest(context?.contentResolver!!, listOf(uri)).intentSender
//                }
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
//                    val recoverableSecurityException = e as RecoverableSecurityException
//                    recoverableSecurityException.userAction.actionIntent.intentSender
//                }
//                else -> null
//            }
//            intentSender?.let { sender ->
//                intentSenderLauncher.launch(
//                    IntentSenderRequest.Builder(sender).build()
//                )
//            }
//        }
        return fileUri
    }

//    private fun registerDeleteResult() {
//        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
//            if (it.resultCode == Activity.RESULT_OK) {
//                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && deleteFileUri != null) {
//                    deleteFileFromStorage(deleteFileUri!!.toFile())
//                }
//                Tools.showToast(requireContext(), "File deleted Successfully")
//            } else {
//                Tools.showToast(requireContext(), "File delete Aborted")
//            }
//        }
//    }

    private fun navbarRecyclerView() {
        navbarRecyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath = filePath.split("/")
                    .dropLastWhile { it != navbarRecyclerAdapter.getItemAt(position).name }
                    .joinToString("/")
                filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
            }
        })
        updateNavigationBarFolderRecyclerView(filePath, rootDir, navbarRecyclerAdapter)
    }

}
