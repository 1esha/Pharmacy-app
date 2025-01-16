package com.example.pharmacyapp.tabs.catalog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.KEY_ARRAY_LIST_CURRENT_ITEMS
import com.example.pharmacyapp.KEY_PATH
import com.example.pharmacyapp.KEY_PATH_MAIN
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentSubdirectoryBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.SubdirectoryAdapter

class SubdirectoryFragment : Fragment() {

    private var _binding: FragmentSubdirectoryBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var navControllerCatalog: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubdirectoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerCatalog = findNavController()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.catalog),
            icon = R.drawable.ic_back,
            onClickNavigationIcon = {
                navControllerCatalog.navigateUp()
            }
        ))

        toolbarViewModel.clearMenu()

        val arrayListItems = arguments?.getStringArrayList(KEY_ARRAY_LIST_CURRENT_ITEMS)?: arrayListOf()
        val pathMain = arguments?.getString(KEY_PATH_MAIN)?: EMPTY_STRING

        val subdirectoryAdapter = SubdirectoryAdapter(listItems = arrayListItems) { path ->
            onClickSubdirectory(pathMain = pathMain, pathSecondary = path)
        }

        rvSubdirectory.adapter = subdirectoryAdapter
        rvSubdirectory.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickSubdirectory(pathMain: String,pathSecondary: String) {
        Log.i("TAG","$pathMain/$pathSecondary")
        val bundle = Bundle()
        val path = "$pathMain/$pathSecondary"
        bundle.putString(KEY_PATH, path)
        navControllerCatalog.navigate(R.id.action_subdirectoryFragment_to_productsFragment, bundle)
    }

}