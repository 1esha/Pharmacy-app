package com.example.pharmacyapp.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentHomeBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        toolbarViewModel.setToolbarSettings(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.main)){})
        toolbarViewModel.setMenuSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}