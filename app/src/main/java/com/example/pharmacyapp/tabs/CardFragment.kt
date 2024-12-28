package com.example.pharmacyapp.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarDataModel
import com.example.pharmacyapp.databinding.FragmentCardBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class CardFragment : Fragment() {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        toolbarViewModel.setToolbarData(toolbarDataModel = ToolbarDataModel(title = getString(R.string.my_card)){})
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}