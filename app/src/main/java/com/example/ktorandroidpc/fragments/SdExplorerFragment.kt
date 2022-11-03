package com.example.ktorandroidpc.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.ktorandroidpc.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SdExplorerFragment : Fragment() {

    private lateinit var fragmentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.id_menu_mobile)?.isVisible = true
        menu.findItem(R.id.id_menu_sd)?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.id_menu_mobile -> {
                Navigation.findNavController(fragmentView).navigate(R.id.sdExplorerFragment_to_explorerFragment)
                true
            }
            R.id.id_menu_computer -> {
                Navigation.findNavController(fragmentView).navigate(R.id.sdExplorerFragment_to_connectPcFragment)
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
        fragmentView = inflater.inflate(R.layout.fragment_sd_explorer, container, false)

        return fragmentView
    }
}