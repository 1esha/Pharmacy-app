package com.example.pharmacyapp.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.pharmacyapp.KEY_ENTER_THE_DATA
import com.example.pharmacyapp.KEY_IS_EXIST
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentRegistrationBinding
import com.example.pharmacyapp.main.viewmodels.RegistrationViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        val registrationViewModel: RegistrationViewModel by viewModels()

        val navControllerMain = findNavController()
        val listCity = listOf(
            getString(R.string.cheboksary),
            getString(R.string.novocheboksarsk)
        )

        val mapMessage = mapOf(KEY_ENTER_THE_DATA to resources.getString(R.string.enter_the_data))

        val sharedPreferences =
            requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)
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
            Log.i("TAG", "RegistrationFragment userInfoModel = $userInfoModel")
            registrationViewModel.setUserInfo(
                userInfoModel = userInfoModel,
                mapMessage = mapMessage
            )
        }

        registrationViewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PendingResult<ResponseModel> -> {}
                is SuccessResult<ResponseModel> -> {
                    if (result.value != null) {
                        val value = result.value ?: throw NullPointerException("RegistrationViewModel result.value = null")

                        if (value.status in 200..299) {
                            successResultListener(
                                navController = navControllerMain,
                                sharedPreferences = sharedPreferences
                            )
                        } else {
                            if (value.message != null) {
                                showToast(context = requireContext(), message = value.message!!)
                            }
                        }
                    }

                }

                is ErrorResult<ResponseModel> -> {
                    errorResultListener(result.exception)
                }
            }
        }

        registrationViewModel.message.observe(viewLifecycleOwner) { message ->
            showToast(context = requireContext(), message = message)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun successResultListener(
        navController: NavController,
        sharedPreferences: SharedPreferences
    ) {
        sharedPreferences.edit().putBoolean(KEY_IS_EXIST, true).apply()
        sharedPreferences.edit().putBoolean(KEY_IS_INIT, false).apply()
        Log.i("TAG", "RegistrationFragment is exist = ${sharedPreferences.getBoolean(KEY_IS_EXIST, false)}")
        navController.navigate(R.id.action_registrationFragment_to_tabsFragment, null, navOptions {
            popUpTo(R.id.initFragment) {
                inclusive = true
            }
        })
    }

    private fun errorResultListener(exception: Exception) {
        showToast(context = requireContext(), message = resources.getString(R.string.error))
    }


    private fun setupCityText(textInputLayout: TextInputLayout, listCity: List<String>) {
        Log.i("TAG", "setupCityText")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        (textInputLayout.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

}