package com.example.pharmacyapp.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.navOptions
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarDataModel
import com.example.pharmacyapp.databinding.FragmentUnauthorizedUserBinding
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.viewmodels.AuthorizedUserViewModel

class UnauthorizedUserFragment : Fragment() {

    private var _binding: FragmentUnauthorizedUserBinding? = null
    private val binding get() = _binding!!

    private val authorizedUserViewModel: AuthorizedUserViewModel by activityViewModels()

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

        bGoToLogInAgain.setOnClickListener {
            navControllerMain.navigate(R.id.nav_graph_log_in, null, navOptions {
                popUpTo(R.id.tabsFragment){
                    inclusive = true
                }
            })
        }

        toolbarViewModel.setToolbarData(toolbarDataModel = ToolbarDataModel(title = getString(R.string.log_in)){})
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        authorizedUserViewModel.setIsShown(isShown = false)
    }

}