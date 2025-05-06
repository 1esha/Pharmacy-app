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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.KEY_USER_NUMBER_PHONE
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_USER
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentLoginBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.LoginViewModel
import com.example.pharmacyapp.main.viewmodels.factories.LoginViewModelFactory
import kotlinx.coroutines.launch


class LoginFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels(
        factoryProducer = { LoginViewModelFactory() }
    )

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    private val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                loginViewModel.stateScreen.collect{ result ->
                    when(result){
                        is Result.Loading -> {
                            onLoadingResultListener()
                        }
                        is Result.Success<*> -> {
                            onSuccessResultListener(data = result.data)
                        }
                        is Result.Error -> {
                            onErrorResultListener(exception = result.exception)
                        }
                    }
                }
            }
        }
    }

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

        layoutSkip.visibility =  if (!sharedPreferences.getBoolean(KEY_IS_INIT,true)) View.GONE else View.VISIBLE

        bLogIn.setOnClickListener {
            val logInModel = LogInModel(
                login = etLogin.text.toString(),
                userPassword = etPassword.text.toString()
            )
            loginViewModel.setIsShownToast()
            loginViewModel.logIn(isNetworkStatus = isNetworkStatus, logInModel = logInModel)
        }

        bGoToRegister.setOnClickListener {
            navControllerMain.navigate(R.id.registrationFragment)
        }

        tvSkipAgain.setOnClickListener {
            onClickSkipAgain()
        }

        layoutPendingResultLogin.bTryAgain.setOnClickListener {
            loginViewModel.tryAgain(isNetworkStatus = isNetworkStatus){
                updateUI(flag = FLAG_SUCCESS_RESULT)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","LoginFragment onSuccessResultListener")
        try {
            if (data == null){
                updateUI(flag = FLAG_SUCCESS_RESULT)
                return
            }
            val _listRequests = data as List<*>
            val listRequests = _listRequests.map { request ->
                return@map request as RequestModel
            }
            Log.i("TAG","listRequests = $listRequests")

            var fullType = ""
            listRequests.forEach { request ->
                fullType += request.type
            }

            when(fullType){
                TYPE_GET_USER -> {
                    val resultGetUser = listRequests.find { it.type == TYPE_GET_USER }?.result!!.asSuccess()!!

                    val responseGetUser= resultGetUser.data as ResponseValueModel<*>

                    val userModel = responseGetUser.value as UserModel

                    val userId = userModel.userId
                    val numberPhone = userModel.userInfoModel.decrypt().phoneNumber

                    sharedPreferences.edit().apply {
                        putBoolean(KEY_IS_INIT, false).apply()
                        putString(KEY_USER_NUMBER_PHONE, numberPhone).apply()
                        putInt(KEY_USER_ID, userId).apply()
                    }

                    Log.i("TAG","LoginFragment onSuccessResultListener userId = ${sharedPreferences.getInt(KEY_USER_ID,-1)}")
                    navControllerMain.navigate(R.id.tabsFragment, null, navOptions {
                        popUpTo(R.id.nav_graph_log_in) {
                            inclusive = true
                        }
                    })
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        loginViewModel.onError(
            exception = exception,
            enterTheData = getString(R.string.enter_the_data),
            toast = {
                getSupportActivity().showToast(message = it ?: getString(R.string.unknown_error))
                updateUI(flag = FLAG_SUCCESS_RESULT)
            }
        ) {
            val message = getErrorMessage(exception = exception)
            updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
        }
    }

    override fun onLoadingResultListener() {
        Log.i("TAG","LoginFragment onPendingResult")

        updateUI(flag = FLAG_PENDING_RESULT)

    }


    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultLogin){
        when(flag) {
            FLAG_PENDING_RESULT -> {
                Log.i("TAG","FLAG_PENDING_RESULT")
                root.visibility = View.VISIBLE
                bTryAgain.visibility = View.INVISIBLE
                tvErrorMessage.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }
            FLAG_SUCCESS_RESULT -> {
                Log.i("TAG","FLAG_SUCCESS_RESULT")
                root.visibility = View.GONE
                bTryAgain.visibility = View.INVISIBLE
                tvErrorMessage.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
            }
            FLAG_ERROR_RESULT -> {
                Log.i("TAG","FLAG_ERROR_RESULT")
                root.visibility = View.VISIBLE
                bTryAgain.visibility = View.VISIBLE
                tvErrorMessage.visibility = View.VISIBLE
                tvErrorMessage.text = messageError
                progressBar.visibility = View.INVISIBLE
            }
        }
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