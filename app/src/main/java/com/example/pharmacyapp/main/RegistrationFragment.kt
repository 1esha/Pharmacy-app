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
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentRegistrationBinding
import com.example.pharmacyapp.main.viewmodels.RegistrationViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception
import kotlin.properties.Delegates


class RegistrationFragment() : Fragment(), ProfileResult {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    override var isShow = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        isShow = false

        val registrationViewModel: RegistrationViewModel by viewModels()

        navControllerMain = findNavController()

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val listCity = listOf(
            getString(R.string.cheboksary),
            getString(R.string.novocheboksarsk)
        )

        setupCityText(
            textInputLayout = binding.layoutCity,
            listCity = listCity
        )

        tvLogin.setOnClickListener {
            navControllerMain.popBackStack()
        }

        bRegister.setOnClickListener {

            isShow = true

            val userInfoModel = UserInfoModel(
                firstName = etFirstName.text.toString(),
                lastName = etLastName.text.toString(),
                email = etEmail.text.toString(),
                phoneNumber = etPhoneNumber.text.toString(),
                userPassword = etPassword.text.toString(),
                city = actvCity.text.toString()
            )
            Log.i("TAG", "RegistrationFragment userInfoModel = $userInfoModel")
            registrationViewModel.createUser(
                userInfoModel = userInfoModel,
                getStringById = ::getStringById
            )
        }

        registrationViewModel.result.observe(viewLifecycleOwner) { result ->
            if (isShow){
                when (result) {
                    is PendingResult<ResponseModel> -> {}
                    is SuccessResult<ResponseModel> -> {
                        if (result.value != null) {
                            val value = result.value ?: throw NullPointerException("RegistrationFragment result.value = null")

                            if (value.status in 200..299) {
                                val userId = registrationViewModel.userId.value?: UNAUTHORIZED_USER
                                onSuccessResultListener(userId = userId)
                            } else {
                                if (value.message != null) {
                                    showToast(context = requireContext(), message = value.message!!)
                                }
                            }
                        }
                    }

                    is ErrorResult<ResponseModel> -> {
                        onErrorResultListener(result.exception)
                    }
                }
            }
        }

        registrationViewModel.message.observe(viewLifecycleOwner) { message ->
            if (isShow){
                showToast(context = requireContext(), message = message)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun getStringById(id: Int): String {
        return resources.getString(id)
    }

    override fun onSuccessResultListener(userId: Int) {
        sharedPreferences.edit().putBoolean(KEY_IS_INIT, false).apply()
        sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
        Log.i("TAG","RegistrationFragment onSuccessResultListener userId = ${sharedPreferences.getInt(
            KEY_USER_ID, UNAUTHORIZED_USER)}")
        navControllerMain.navigate(R.id.action_registrationFragment_to_tabsFragment, null, navOptions {
            popUpTo(R.id.initFragment) {
                inclusive = true
            }
        })
    }

    override fun onErrorResultListener(exception: Exception) {
        showToast(context = requireContext(), message = resources.getString(R.string.error))
    }


    private fun setupCityText(textInputLayout: TextInputLayout, listCity: List<String>) {
        Log.i("TAG", "setupCityText")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        (textInputLayout.editText as? MaterialAutoCompleteTextView)?.setAdapter(adapter)
    }

}