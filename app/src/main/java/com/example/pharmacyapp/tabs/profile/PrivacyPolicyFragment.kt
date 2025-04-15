package com.example.pharmacyapp.tabs.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentPrivacyPolicyBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var navControllerProfile: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerProfile = findNavController()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.privacy_policy),
            icon = R.drawable.ic_back
        ) {
            navControllerProfile.navigateUp()
        })
        toolbarViewModel.clearMenu()

        webViewPrivacyPolicy.loadUrl("file:///android_asset/privacy_policy.html")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}