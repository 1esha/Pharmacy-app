package com.example.pharmacyapp.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentBasketBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class BasketFragment : Fragment() {

    private var _binding: FragmentBasketBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasketBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        toolbarViewModel.setToolbarSettings(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.basket)){})
        toolbarViewModel.setMenuSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}