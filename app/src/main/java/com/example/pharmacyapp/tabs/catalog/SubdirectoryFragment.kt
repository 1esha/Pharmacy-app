package com.example.pharmacyapp.tabs.catalog

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.KEY_ARRAY_LIST_CURRENT_ITEMS
import com.example.pharmacyapp.KEY_PATH_MAIN
import com.example.pharmacyapp.databinding.FragmentSubdirectoryBinding
import com.example.pharmacyapp.tabs.catalog.adapters.SubdirectoryAdapter

class SubdirectoryFragment : Fragment() {

    private var _binding: FragmentSubdirectoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubdirectoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

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

    }

}