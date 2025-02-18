package com.example.pharmacyapp.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentTabsBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class TabsFragment : Fragment() {

    private var _binding: FragmentTabsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        val toolbarViewModel: ToolbarViewModel by activityViewModels()
        val navHostTabs =
            childFragmentManager.findFragmentById(R.id.navHostFragmentTabs) as NavHostFragment
        val navControllerTabs = navHostTabs.navController

        bnvTabs.setupWithNavController(navControllerTabs)

        toolbarViewModel.toolbarSettings.observe(viewLifecycleOwner) { toolbarSettingsModel ->
            with(toolbarSettingsModel) {
                toolbarTabs.setTitle(title)
                if (icon != null) toolbarTabs.setNavigationIcon(icon) else toolbarTabs.navigationIcon =
                    null

                toolbarTabs.setNavigationOnClickListener {
                    onClickNavigationIcon()
                }
            }

        }

        toolbarViewModel.menu.observe(viewLifecycleOwner) { menu ->
            toolbarTabs.menu.clear()
            if (menu != null) {
                toolbarTabs.inflateMenu(menu)
            }
        }

        toolbarViewModel.menuClickListener.observe(viewLifecycleOwner) { listener ->
            if (listener != null) {
                toolbarTabs.setOnMenuItemClickListener { menuItem ->
                    listener(menuItem.itemId)
                    true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}