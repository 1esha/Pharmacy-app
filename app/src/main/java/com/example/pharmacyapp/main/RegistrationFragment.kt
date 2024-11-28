package com.example.pharmacyapp.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentRegistrationBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){

        val navControllerMain = findNavController()
        val listCity = listOf("Чебоксары","Новочебоксарск")

        setupCityText(
            textInputLayout = binding.layoutCity,
            listCity = listCity
        )

        tvLogin.setOnClickListener {
            navControllerMain.popBackStack()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCityText(textInputLayout:TextInputLayout, listCity:List<String>){
        Log.i("TAG","setupCityText")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        (textInputLayout.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

}