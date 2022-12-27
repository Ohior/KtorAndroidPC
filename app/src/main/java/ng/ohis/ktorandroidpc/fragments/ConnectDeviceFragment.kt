package ng.ohis.ktorandroidpc.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import androidx.activity.addCallback
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
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
import java.util.concurrent.atomic.AtomicBoolean


class ConnectDeviceFragment : Fragment(), ExplorerInterface {
    private lateinit var fragmentView: View
    private lateinit var idRvRootFolder: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var idNavigateRecyclerView: RecyclerView
    private lateinit var navbarRecyclerAdapter: NavbarRecyclerAdapter

    private lateinit var mManager: WifiP2pManager
    private lateinit var wifiManager: WifiManager
    private lateinit var mChannel: WifiP2pManager.Channel
    private var mReceiver: BroadcastReceiver? = null

    private lateinit var mIntentFilter: IntentFilter
    private lateinit var connected: AtomicBoolean

    private lateinit var rootDir: StorageDataClass
    private var filePath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_connect_device, container, false)

        fragmentInitializers()

        fragmentExecutables()

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

    private fun fragmentInitializers() {
        rootDir = Gson().fromJson(
            requireArguments().getString(Const.FRAGMENT_DATA_KEY),
            StorageDataClass::class.java
        )
        idRvRootFolder = fragmentView.findViewById(R.id.id_rv_folder)
        recyclerAdapter = RecyclerAdapter(
            requireContext(),
            idRvRootFolder,
            R.layout.explorer_item,
            menuVisibility = false
        )
        idNavigateRecyclerView = fragmentView.findViewById(R.id.id_rv_navigate)
        navbarRecyclerAdapter = NavbarRecyclerAdapter(requireContext(), idNavigateRecyclerView)
        filePath = rootDir.rootDirectory
        filePath = navigateDirectoryForward(null, recyclerAdapter, requireContext(), filePath)
    }

    private fun fragmentExecutables() {
        // this makes sure pressing the back button only exit
        // this fragment when user is at root directory
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            filePath = navigateDirectoryBackward(recyclerAdapter, rootDir.rootDirectory, filePath)
            if (filePath.isEmpty()) {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }

        recyclerViewClickListener()

        inflateMenuItem()
    }

    private fun recyclerViewClickListener() {
        recyclerAdapter.onClickListener(object : OnClickInterface {
            override fun onItemClick(position: Int, view: View) {
                filePath =
                    navigateDirectoryForward(position, recyclerAdapter, requireContext(), filePath)
            }
        })
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
                menu.findItem(R.id.id_menu_connect_device).isVisible = false
                menu.findItem(R.id.id_menu_mobile).isVisible = false
                menu.findItem(R.id.id_menu_sd).isVisible = false
                menu.findItem(R.id.id_menu_computer).isVisible = true

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.id_menu_computer -> {
                        Tools.navigateFragmentToFragment(
                            this@ConnectDeviceFragment,
                            R.id.connectDeviceFragment_to_connectPcFragment
                        )
                        true
                    }
                    else -> false
                }
            }

        })
    }
}
