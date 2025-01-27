package com.example.pharmacyapp.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_EMPTY
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentLoginBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LoginFragment : Fragment(), ProfileResult {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerMain = findNavController()

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        layoutSkip.visibility =  if (!sharedPreferences.getBoolean(KEY_IS_INIT,true)) View.GONE else View.VISIBLE

        bLogIn.setOnClickListener {
            onSuccessfulEvent(type = TYPE_EMPTY) {
                loginViewModel.setIsShown(isShown = false)
                val logInModel = LogInModel(
                    login = etLogin.text.toString(),
                    userPassword = etPassword.text.toString()
                )
                loginViewModel.setLogInData(logInModel = logInModel)
            }

        }
        bGoToRegister.setOnClickListener {
            navControllerMain.navigate(R.id.registrationFragment)
        }

        tvSkipAgain.setOnClickListener {
            onClickSkipAgain()
        }

        loginViewModel.result.observe(viewLifecycleOwner){ result ->
            when (result) {
                is PendingResult -> { onPendingResultListener() }
                is SuccessResult-> {
                    if (result.value != null) {
                        val value = result.value ?: throw NullPointerException("LoginFragment result.value = null")
                        val userId = value.value?.userId ?: UNAUTHORIZED_USER
                        onSuccessResultListener(userId = userId, value = value)
                    }

                }

                is ErrorResult -> {
                    val errorType = loginViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(userId: Int, value: T, type: String?) {
        val responseValueModel = value as ResponseValueModel<*>
        val status = responseValueModel.responseModel.status
        val message = responseValueModel.responseModel.message
        viewLifecycleOwner.lifecycleScope.launch {
            delay(300)

            updateUI(flag = FLAG_SUCCESS_RESULT)

            if (status in 200..299){
                sharedPreferences.apply {
                    edit().putBoolean(KEY_IS_INIT, false).apply()
                    edit().putInt(KEY_USER_ID, userId).apply()
                }

                Log.i("TAG","LoginFragment onSuccessResultListener userId = ${sharedPreferences.getInt(KEY_USER_ID,-1)}")
                navControllerMain.navigate(R.id.tabsFragment, null, navOptions {
                    popUpTo(R.id.nav_graph_log_in) {
                        inclusive = true
                    }
                })

            }
            else{
                val isShown = loginViewModel.isShown.value?:throw NullPointerException("LoginFragment onSuccessResultListener isShown = null")
                if (!isShown){
                    if (message != null) getSupportActivity().showToast(message = message)
                }

            }

            loginViewModel.setIsShown(isShown = true)
        }

    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        val isShown = loginViewModel.isShown.value?:throw NullPointerException("LoginFragment onErrorResultListener isShown = null")
        if (!isShown){
            getSupportActivity().showToast(message = message)
        }
        loginViewModel.setIsShown(isShown = true)
        updateUI(flag = FLAG_ERROR_RESULT)

    }

    override fun onPendingResultListener() {
        Log.i("TAG","LoginFragment onPendingResult")

        updateUI(flag = FLAG_PENDING_RESULT)
        loginViewModel.clearErrorType()

    }

    override fun onSuccessfulEvent(
        type: String,
        exception: java.lang.Exception?,
        onSuccessfulEventListener: () -> Unit
    ) {
        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())
        val network = Network()

        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                loginViewModel.setResult(result = PendingResult())
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()
                loginViewModel.setResult(result = ErrorResult(exception = currentException), errorType = errorType)
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding) {
        when(flag) {
            FLAG_PENDING_RESULT -> {
                progressBarLogIn.visibility = View.VISIBLE
                setEnable(isEnabled = false)
            }
            FLAG_SUCCESS_RESULT -> {
                binding.progressBarLogIn.visibility = View.INVISIBLE
                setEnable(isEnabled = true)
            }
            FLAG_ERROR_RESULT -> {
                progressBarLogIn.visibility = View.INVISIBLE
                setEnable(isEnabled = true)
            }
        }
    }

    private fun setEnable(isEnabled: Boolean) = with(binding){
        etLogin.isEnabled = isEnabled
        etPassword.isEnabled = isEnabled
        bLogIn.isEnabled = isEnabled
    }

    private fun onClickSkipAgain(){
        sharedPreferences.apply {
            edit().putBoolean(KEY_IS_INIT,false).apply()
            edit().putInt(KEY_USER_ID, UNAUTHORIZED_USER).apply()
        }
        navControllerMain.navigate(R.id.tabsFragment, null, navOptions {
            popUpTo(R.id.nav_graph_log_in){
                inclusive = true
            }
        })
    }

}