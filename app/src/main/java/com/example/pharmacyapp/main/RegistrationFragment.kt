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
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettings
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentRegistrationBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.RegistrationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RegistrationFragment() : Fragment(), ProfileResult {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val registrationViewModel: RegistrationViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        val network = Network()

        val toolbarSettings = ToolbarSettings(toolbar = binding.layoutToolbarMainRegistration.toolbarMain)

        navControllerMain = findNavController()

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        toolbarSettings.installToolbarMain(icon = R.drawable.ic_back) {
            navControllerMain.navigateUp()
        }

        tvLogin.setOnClickListener {
            navControllerMain.popBackStack()
        }

        bRegister.setOnClickListener {

            registrationViewModel.setIsShown(isShown = false)

            val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())

            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    registrationViewModel.setResult(result = PendingResult())
                    val userInfoModel = UserInfoModel(
                        firstName = etFirstName.text.toString(),
                        lastName = etLastName.text.toString(),
                        email = etEmail.text.toString(),
                        phoneNumber = etPhoneNumber.text.toString(),
                        userPassword = etPassword.text.toString(),
                        city = actvCity.text.toString()
                    )
                    Log.i("TAG", "RegistrationFragment userInfoModel = $userInfoModel")
                    registrationViewModel.createUser(userInfoModel = userInfoModel)
                },
                disconnectionListener = {
                    registrationViewModel.setResult(
                        result = ErrorResult(exception = Exception()),
                        errorType = DisconnectionError()
                    )
                }
            )

        }

        registrationViewModel.isSetupCity.observe(viewLifecycleOwner) {
            setupCityText()
        }

        registrationViewModel.resultCreateUser.observe(viewLifecycleOwner) { result ->

            when (result) {
                is PendingResult -> { onPendingResult() }
                is SuccessResult -> {
                    val value = result.value?: throw NullPointerException("RegistrationFragment value = null")
                    val userId = registrationViewModel.userId.value?: UNAUTHORIZED_USER
                    onSuccessResultListener(userId = userId, value = value)
                }
                is ErrorResult -> {
                    val errorType = registrationViewModel.errorType.value
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
        viewLifecycleOwner.lifecycleScope.launch {
            val responseModel = value as ResponseModel
            val status = responseModel.status
            val message = responseModel.message
            delay(300)
            binding.bRegister.isEnabled = true
            setEnable(isEnabled = true)
            binding.progressBarRegistration.visibility = View.INVISIBLE
            if (status in 200..299){
                sharedPreferences.edit().putBoolean(KEY_IS_INIT, false).apply()
                sharedPreferences.edit().putInt(KEY_USER_ID, userId).apply()
                Log.i("TAG","RegistrationFragment onSuccessResultListener userId = ${sharedPreferences.getInt(
                    KEY_USER_ID, UNAUTHORIZED_USER)}")
                navControllerMain.navigate(R.id.tabsFragment, null, navOptions {
                    popUpTo(R.id.nav_graph_log_in) {
                        inclusive = true
                    }
                })
            }
            else{
                val isShown = registrationViewModel.isShown.value?: throw NullPointerException("RegistrationFragment onSuccessResultListener isShown = null")
                if (!isShown){
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            registrationViewModel.setIsShown(isShown = true)

        }
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        viewLifecycleOwner.lifecycleScope.launch{
            delay(300)
            binding.bRegister.isEnabled = true
            setEnable(isEnabled = true)
            binding.progressBarRegistration.visibility = View.INVISIBLE
            val isShown = registrationViewModel.isShown.value?: throw NullPointerException("RegistrationFragment onErrorResultListener isShown = null")
            if (!isShown) getSupportActivity().showToast(message = message)

            registrationViewModel.setIsShown(isShown = true)
        }

    }

    override fun onPendingResult() {
        binding.bRegister.isEnabled = false
        binding.progressBarRegistration.visibility = View.VISIBLE
        setEnable(isEnabled = false)
        registrationViewModel.clearErrorType()
        Log.i("TAG","RegistrationFragment onPendingResult")
    }

    private fun setEnable(isEnabled: Boolean) = with(binding){
        etFirstName.isEnabled = isEnabled
        etLastName.isEnabled = isEnabled
        etEmail.isEnabled = isEnabled
        etPhoneNumber.isEnabled = isEnabled
        etPassword.isEnabled = isEnabled
        layoutCity.isEnabled = isEnabled
    }


    private fun setupCityText() {

        val listCity = listOf(
            getString(R.string.cheboksary),
            getString(R.string.novocheboksarsk)
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        binding.actvCity.setAdapter(adapter)
    }

}