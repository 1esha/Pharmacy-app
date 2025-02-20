package com.example.pharmacyapp.tabs.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.models.DetailsProductModel
import com.example.pharmacyapp.KEY_ARRAY_LIST_BODY_INSTRUCTION
import com.example.pharmacyapp.KEY_ARRAY_LIST_TITLES_INSTRUCTION
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentInstructionManualBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.InstructionManualAdapter

class InstructionManualFragment: Fragment() {

    private var _binding: FragmentInstructionManualBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var navControllerCatalog: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navControllerCatalog = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInstructionManualBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        val arrayListTitles = arguments?.getStringArrayList(KEY_ARRAY_LIST_TITLES_INSTRUCTION) ?:
        throw NullPointerException("InstructionManualFragment arrayListTitles = null")

        val arrayListBody = arguments?.getStringArrayList(KEY_ARRAY_LIST_BODY_INSTRUCTION) ?:
        throw NullPointerException("InstructionManualFragment arrayListBody = null")

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.instruction_manual),
            icon = R.drawable.ic_back
        ) {
            navControllerCatalog.navigateUp()
        })

        toolbarViewModel.clearMenu()

        val listDetailsProduct = getListDetailsProduct(
            arrayListTitles = arrayListTitles,
            arrayListBody = arrayListBody
        )

        val instructionManualAdapter = InstructionManualAdapter(listDetailsProduct = listDetailsProduct)

        rvInstructionManual.adapter = instructionManualAdapter
        rvInstructionManual.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // преобразование arrayListTitles и arrayListBody в listDetailsProduct
    private fun getListDetailsProduct(
        arrayListTitles: ArrayList<String>,
        arrayListBody: ArrayList<String>): List<DetailsProductModel> {

        val numberInstructions = arrayListTitles.size - 1

        val mutableListDetailsProduct = mutableListOf<DetailsProductModel>()
            for (index in 0..numberInstructions) {
                val title = arrayListTitles[index]
                val body = arrayListBody[index]
                val detailsProductModel = DetailsProductModel(
                    title = title,
                    body = body
                )

                mutableListDetailsProduct.add(detailsProductModel)
            }

        return mutableListDetailsProduct
    }
}