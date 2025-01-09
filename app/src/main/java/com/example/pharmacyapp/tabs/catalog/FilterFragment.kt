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
import com.example.pharmacyapp.KEY_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentFilterBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var navControllerCatalog: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        val arrayListIdsAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()

        Log.i("TAG","FilterFragment arrayListIdsAddresses = $arrayListIdsAddresses")

        navControllerCatalog = findNavController()

        toolbarViewModel.setToolbarSettings(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.filters),
            icon = R.drawable.ic_back
        ) {
            navControllerCatalog.navigateUp()
        })
        toolbarViewModel.setMenuSettings()

        layoutAvailabilityInPharmacies.setOnClickListener {
            navControllerCatalog.navigate(R.id.action_filterFragment_to_pharmacyAddressesFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}