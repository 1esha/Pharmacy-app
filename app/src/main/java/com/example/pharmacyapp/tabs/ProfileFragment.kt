package com.example.pharmacyapp.tabs

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val navHostProfile = childFragmentManager.findFragmentById(R.id.navHostFragmentProfile) as NavHostFragment
        val navControllerProfile = navHostProfile.navController

        val navGraphProfile = navControllerProfile.navInflater.inflate(R.navigation.nav_graph_profile)
        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        val isInit = savedInstanceState?.getBoolean(KEY_IS_INIT) ?: INIT

        if (isInit) {

            if (userId == UNAUTHORIZED_USER){
                navGraphProfile.setStartDestination(R.id.unauthorizedUserFragment)
            }
            else{
                navGraphProfile.setStartDestination(R.id.authorizedUserFragment)
            }

            navControllerProfile.graph = navGraphProfile
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_INIT,false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val INIT = true
        const val KEY_IS_INIT = "KEY_IS_INIT"
    }

}