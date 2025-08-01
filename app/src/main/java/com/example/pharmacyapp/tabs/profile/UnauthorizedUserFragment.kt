package com.example.pharmacyapp.tabs.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.navOptions
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentUnauthorizedUserBinding
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel

class UnauthorizedUserFragment : Fragment() {

    private var _binding: FragmentUnauthorizedUserBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnauthorizedUserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){

        val navControllerMain  = getSupportActivity().getNavControllerMain()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.log_in)){})
        toolbarViewModel.clearMenu()

        bGoToLogInAgain.setOnClickListener {
            navControllerMain.navigate(R.id.nav_graph_log_in, null, navOptions {
                popUpTo(R.id.tabsFragment){
                    inclusive = true
                }
            })
        }

        tvVersionName.text = getSupportActivity().getVersionName()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}