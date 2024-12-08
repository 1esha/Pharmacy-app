package com.example.pharmacyapp.main

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.KEY_IS_EXIST
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentLoginBinding
import com.example.pharmacyapp.main.viewmodels.LoginViewModel
import java.lang.Exception
import kotlin.properties.Delegates


class LoginFragment : Fragment(), ProfileResult {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    override var isShow: Boolean by Delegates.notNull()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        isShow = false

        val loginViewModel: LoginViewModel by viewModels()

        navControllerMain = findNavController()

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        bLogIn.setOnClickListener {

            isShow = true

            val logInModel = LogInModel(
                login = etLogin.text.toString(),
                userPassword = etPassword.text.toString()
            )
            loginViewModel.setLogInData(
                logInModel = logInModel,
                getStringById = ::getStringById
            )

        }
        bGoToRegister.setOnClickListener {
            navControllerMain.navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        loginViewModel.result.observe(viewLifecycleOwner){ result ->

            if (isShow){
                when (result) {
                    is PendingResult<ResponseValueModel<UserModel>> -> {}
                    is SuccessResult<ResponseValueModel<UserModel>> -> {
                        if (result.value != null) {
                            val value = result.value ?: throw NullPointerException("LoginFragment result.value = null")

                            if (value.responseModel.status in 200..299) {
                                onSuccessResultListener()
                            } else {
                                if (value.responseModel.message != null) {
                                    showToast(context = requireContext(), message = value.responseModel.message!!)
                                }
                            }
                        }

                    }

                    is ErrorResult<ResponseValueModel<UserModel>> -> {
                        onErrorResultListener(result.exception)
                    }
                }
            }
        }
        loginViewModel.message.observe(viewLifecycleOwner){ message ->
            if (isShow){
                showToast(
                    context = requireContext(),
                    message = message
                )
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

    override fun onSuccessResultListener() {
        sharedPreferences.edit().putBoolean(KEY_IS_EXIST, true).apply()
        sharedPreferences.edit().putBoolean(KEY_IS_INIT, false).apply()
        Log.i("TAG", "LoginFragment is exist = ${sharedPreferences.getBoolean(KEY_IS_EXIST, false)}")
        navControllerMain.navigate(R.id.action_loginFragment_to_tabsFragment, null, navOptions {
            popUpTo(R.id.initFragment) {
                inclusive = true
            }
        })
    }

    override fun onErrorResultListener(exception: Exception) {
        showToast(context = requireContext(), message = resources.getString(R.string.error))
    }

}