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
import com.example.domain.Network
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentLoginBinding
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.LoginViewModel
import java.lang.Exception
import kotlin.properties.Delegates


class LoginFragment : Fragment(), ProfileResult<ResponseValueModel<UserModel>> {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    override var isShow = false

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

        val network = Network()

        navControllerMain = findNavController()

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        bLogIn.setOnClickListener {

            val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())

            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    isShow = true

                    val logInModel = LogInModel(
                        login = etLogin.text.toString(),
                        userPassword = etPassword.text.toString()
                    )
                    loginViewModel.setLogInData(
                        logInModel = logInModel,
                        getStringById = { id ->
                            resources.getString(id)
                        }
                    )
                },
                disconnectionListener = {
                    getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
                }
            )

        }
        bGoToRegister.setOnClickListener {
            navControllerMain.navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        loginViewModel.result.observe(viewLifecycleOwner){ result ->

            if (isShow){
                when (result) {
                    is PendingResult<ResponseValueModel<UserModel>> -> { onPendingResult() }
                    is SuccessResult<ResponseValueModel<UserModel>> -> {
                        if (result.value != null) {
                            val value = result.value ?: throw NullPointerException("LoginFragment result.value = null")
                            val userId = value.value?.userId ?: UNAUTHORIZED_USER
                            onSuccessResultListener(userId = userId, value = value)
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
                getSupportActivity().showToast(message = message)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onSuccessResultListener(userId: Int, value: ResponseValueModel<UserModel>) {
        val status = value.responseModel.status
        val message = value.responseModel.message
        if (status in 200..299){
            sharedPreferences.edit().putBoolean(KEY_IS_INIT, false).apply()
            sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
            Log.i("TAG","LoginFragment onSuccessResultListener userId = ${sharedPreferences.getInt(
                KEY_USER_ID,-1)}")
            navControllerMain.navigate(R.id.action_loginFragment_to_tabsFragment, null, navOptions {
                popUpTo(R.id.initFragment) {
                    inclusive = true
                }
            })
        }
        else{
            if (message != null) getSupportActivity().showToast(message = message)
        }

    }

    override fun onErrorResultListener(exception: Exception) {
        getSupportActivity().showToast(message = resources.getString(R.string.error))
    }

    override fun onPendingResult() {
        Log.i("TAG","LoginFragment onPendingResult")
    }

}