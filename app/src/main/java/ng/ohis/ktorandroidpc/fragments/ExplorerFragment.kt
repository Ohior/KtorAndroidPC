package ng.ohis.ktorandroidpc.fragments

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ng.ohis.ktorandroidpc.MainActivity
import ng.ohis.ktorandroidpc.R
import ng.ohis.ktorandroidpc.adapter.*
import ng.ohis.ktorandroidpc.classes.ExplorerInterface
import ng.ohis.ktorandroidpc.explorer.FileType
import ng.ohis.ktorandroidpc.openFileWithDefaultApp
import ng.ohis.ktorandroidpc.popUpWindow
import ng.ohis.ktorandroidpc.utills.Const
import ng.ohis.ktorandroidpc.utills.Tools

open class ExplorerFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idNavigateRecyclerView: RecyclerView
    private lateinit var navbarRecyclerAdapter: NavbarRecyclerAdapter
    private lateinit var idToolbarTextView: TextView
    private lateinit var rootDir: StorageDataClass
    private var filePath = ""
    private var deleteFileUri: Uri? = null

    private var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
//                    deleteFileUri?.let { it1 -> Tools.deleteFileFromStorage(it1.toFile()) }
                    deleteFileUri = Tools.deleteFileFromStorage(
                        file = Uri.parse("deleteFileUri").toFile(),
                        context = requireContext()
                    ) {
                    }
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
        idToolbarTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
            R.drawable.ic_arrow_left,
            0,
            0,
            0
        )
        idToolbarTextView.setOnClickListener {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                requireActivity().onBackPressed()
            }
        }

        // this makes sure pressing the back button only exit
        // this fragment when user is at root directory
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }

        inflateMenuItem()
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
                                        deleteFileUri = Tools.deleteFileFromStorage(
                                            file = fileModel.file,
                                            context = requireContext()
                                        ) {
                                            intentSenderLauncher.launch(
                                                IntentSenderRequest.Builder(
                                                    it
                                                ).build()
                                            )
                                        }
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

    private fun variableInitializers() {
        rootDir = Gson().fromJson(
            requireArguments().getString(Const.FRAGMENT_DATA_KEY),
            StorageDataClass::class.java
        )
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
        idToolbarTextView = requireActivity().findViewById(R.id.id_tv_toolbar)
        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
        filePath = rootDir.rootDirectory
        recyclerAdapter = RecyclerAdapter(requireContext(), idRvRootFolder, R.layout.explorer_item)
        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)
        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
        idToolbarTextView.text = getToolbarName(rootDir, requireActivity())
    }

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

    private fun inflateMenuItem() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.main_menu, menu)
                menu.findItem(R.id.id_menu_computer)?.isVisible = true
                hideAndShowMenuItem(menu, rootDir)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.id_menu_connect_device ->{
                        Tools.navigateFragmentToFragment(
                            this@ExplorerFragment,
                            R.id.explorerFragment_to_connectDeviceFragment
                        )
                        true
                    }
                    R.id.id_menu_computer -> {
                        Tools.navigateFragmentToFragment(
                            this@ExplorerFragment,
                            R.id.explorerFragment_to_connectPcFragment
                        )
                        true
                    }
                    R.id.id_menu_sd -> {
                        rootDir = StorageDataClass(
                            rootDirectory = Tools.getExternalSDCardRootDirectory(requireActivity())!!,
                            isSdStorage = true
                        )
                        filePath = navigateDirectoryForward(
                            null,
                            recyclerAdapter,
                            requireContext(),
                            rootDir.rootDirectory
                        )
                        idToolbarTextView.text = getToolbarName(rootDir, requireActivity())
                        true
                    }
                    R.id.id_menu_mobile -> {
                        rootDir = StorageDataClass(
                            rootDirectory = Const.ROOT_PATH,
                            isSdStorage = false
                        )
                        filePath = navigateDirectoryForward(
                            null,
                            recyclerAdapter,
                            requireContext(),
                            rootDir.rootDirectory
                        )
                        idToolbarTextView.text = getToolbarName(rootDir, requireActivity())
                        true
                    }
                    else -> false
                }
            }

        })
    }

}