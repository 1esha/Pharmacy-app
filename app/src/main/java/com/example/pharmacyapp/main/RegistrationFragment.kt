package com.example.pharmacyapp.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.pharmacyapp.KEY_IS_EXIST
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentRegistrationBinding
import com.example.pharmacyapp.main.viewmodels.RegistrationViewModel
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

        val registrationViewModel: RegistrationViewModel by viewModels()

        val navControllerMain = findNavController()
        val listCity = listOf("Чебоксары","Новочебоксарск")

        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES,Context.MODE_PRIVATE)
        setupCityText(
            textInputLayout = binding.layoutCity,
            listCity = listCity
        )

        tvLogin.setOnClickListener {
            navControllerMain.popBackStack()
        }

        bRegister.setOnClickListener {
            val userInfoModel = UserInfoModel(
                firstName = etFirstName.text.toString(),
                lastName = etLastName.text.toString(),
                email = etEmail.text.toString(),
                phoneNumber = etPhoneNumber.text.toString(),
                userPassword = etPassword.text.toString(),
                city = actvCity.text.toString()
            )
            registrationViewModel.setUserInfo(userInfoModel = userInfoModel)
        }

        registrationViewModel.result.observe(viewLifecycleOwner){ result ->
            when(result){
                is PendingResult<ResponseModel> -> {}
                is SuccessResult<ResponseModel> -> {
                    sharedPreferences.edit().putBoolean(KEY_IS_EXIST, true).apply()
                    Log.i("TAG","RegistrationFragment is exist = ${sharedPreferences.getBoolean(KEY_IS_EXIST,false)}")
                    navControllerMain.navigate(R.id.action_registrationFragment_to_tabsFragment, null, navOptions {
                        popUpTo(R.id.registrationFragment){
                            inclusive = true
                        }
                    })
                }
                is ErrorResult<ResponseModel> -> {
                    showToast(
                        context = requireContext(),
                        message = "Ошибка")
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(context:Context, message: String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }


    private fun setupCityText(textInputLayout:TextInputLayout, listCity:List<String>){
        Log.i("TAG","setupCityText")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        (textInputLayout.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

}