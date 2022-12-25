package ng.ohis.ktorandroidpc.fragments

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.whenResumed
import androidx.lifecycle.withStarted
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ng.ohis.ktorandroidpc.BuildConfig
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.SettingsActivity
import ng.ohis.ktorandroidpc.adapter.*
import ng.ohis.ktorandroidpc.classes.ExplorerInterface
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.utills.*
import java.io.File

class ExplorerFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idNavigateRecyclerView: RecyclerView
    private lateinit var navbarRecyclerAdapter: NavbarRecyclerAdapter
    private lateinit var idToolbar: Toolbar
    private lateinit var rootDir: StorageDataClass
    private var filePath = ""
    private var deleteFileUri: Uri? = null
    private lateinit var storageName:String

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


    override fun onResume() {
        navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_explorer, container, false)

        fragmentInitializers()

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
        idToolbar.setOnClickListener {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                requireActivity().onBackPressed()
            }
        }

        // this call back prevent fragment from repeating them selves
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }

        Tools.inflateMenuItem(idToolbar,
            settingMenu = {
                // SETTINGS MENU ITEM PRESSED
                requireActivity().startActivity(
                    Intent(
                        activity,
                        SettingsActivity::class.java
                    )
                )
            },
            sdCardMenu = {
                // SD CARD MENU ITEM PRESSED
                rootDir = StorageDataClass(
                    rootDirectory = Tools.getExternalSDCardRootDirectory(requireActivity())!!,
                    isSdStorage = true,
                    title = Const.SD_CARD
                )
                filePath = navigateDirectoryForward(
                    null,
                    recyclerAdapter,
                    requireContext(),
                    rootDir.rootDirectory
                )
                idToolbar = Tools.manageTopNav(fragmentView, Const.SD_CARD)
            },
            localStorageMenu = {
                // LOCAL STORAGE MENU ITEM PRESSED
                rootDir = StorageDataClass(
                    rootDirectory = Const.ROOT_PATH,
                    isSdStorage = false,
                    Const.LOCAL_STORAGE
                )
                filePath = navigateDirectoryForward(
                    null,
                    recyclerAdapter,
                    requireContext(),
                    rootDir.rootDirectory
                )
                idToolbar = Tools.manageTopNav(fragmentView, Const.LOCAL_STORAGE)
            },
            connectComputerMenu = {
                Tools.navigateFragmentToFragment(
                    fragmentView,
                    R.id.explorerFragment_to_connectPcFragment
                )
            }
        )
    }

    private fun recyclerViewClickListener() {
        recyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath =
                    navigateDirectoryForward(position, recyclerAdapter, requireContext(), filePath)
            }

            override fun onMenuClick(fileModel: FileModel, view: View, position: Int) {
                // when recycler view menu is clicked, display drop down menu
                PopupMenu(context, view).apply {
                    this.inflate(R.menu.rv_menu_item)
                    // TODO: 28/11/2022 create delete execution for sd card
                    if (rootDir.isSdStorage) menu.findItem(R.id.id_rv_menu_delete)?.isVisible =
                        false
                    this.setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.id_rv_menu_delete -> {
                                requireContext().popUpWindow(
                                    "This action is permanent",
                                    "Delete File"
                                ) { builder ->
                                    builder.setPositiveButton("Delete") { _, _ ->
                                        deleteFileUri = deleteFileFromStorage(fileModel.file)
                                        filePath =
                                            navigateDirectoryForward(
                                                null,
                                                recyclerAdapter,
                                                requireContext(),
                                                filePath
                                            )
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
                                    Tools.showToast(
                                        requireContext(),
                                        "No App To open this File! 😢"
                                    )
                                } else filePath =
                                    navigateDirectoryForward(
                                        null,
                                        recyclerAdapter,
                                        requireContext(),
                                        filePath
                                    )
                                true
                            }
                            R.id.id_rv_menu_detail -> {
                                requireContext().popUpWindow(
                                    title = "Properties : ",
                                    message = MenuDetailDataClass(fileModel).toString()
                                ) { it.setCancelable(true) }
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

    private fun fragmentInitializers() {
        rootDir = StorageDataClass(Const.ROOT_PATH, false, Const.LOCAL_STORAGE).apply {
            idToolbar = Tools.manageTopNav(fragmentView, title ?: getString(R.string.app_name))
        }
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
        filePath = rootDir.rootDirectory
        recyclerAdapter = RecyclerAdapter(requireContext(), idRvRootFolder, R.layout.explorer_item)
        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)
        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
    }

    private fun deleteFileFromStorage(file: File): Uri {
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )
        try {
            when {
                file.isFile -> File(file.absolutePath).delete()
                file.isDirectory -> File(file.absolutePath).deleteRecursively()
            }
        } catch (e: SecurityException) {
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(
                        requireActivity().contentResolver,
                        listOf(fileUri)
                    ).intentSender
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
                filePath =
                    navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
            }
        })
        updateNavigationBarFolderRecyclerView(filePath, rootDir, navbarRecyclerAdapter)
    }
}