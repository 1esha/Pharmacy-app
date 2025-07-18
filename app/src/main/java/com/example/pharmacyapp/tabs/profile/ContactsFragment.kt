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
import com.example.pharmacyapp.databinding.FragmentContactsBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var navControllerProfile: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerProfile = findNavController()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.contacts),
            icon = R.drawable.ic_back
        ) {
            navControllerProfile.navigateUp()
        })
        toolbarViewModel.clearMenu()

        webViewContacts.loadUrl("file:///android_asset/contacts.html")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}