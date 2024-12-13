package com.example.pharmacyapp.tabs

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentUnauthorizedUserBinding
import com.example.pharmacyapp.getSupportActivity

class UnauthorizedUserFragment : Fragment() {

    private var _binding: FragmentUnauthorizedUserBinding? = null
    private val binding get() = _binding!!

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
            navControllerMain.navigate(R.id.loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}