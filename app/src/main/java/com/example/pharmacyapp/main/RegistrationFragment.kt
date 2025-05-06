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
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.KEY_USER_NUMBER_PHONE
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_CREATE_USER
import com.example.pharmacyapp.TYPE_GET_USER_ID
import com.example.pharmacyapp.ToolbarSettings
import com.example.pharmacyapp.databinding.FragmentRegistrationBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.RegistrationViewModel
import com.example.pharmacyapp.main.viewmodels.factories.RegistrationViewModelFactory
import kotlinx.coroutines.launch


class RegistrationFragment() : Fragment(), ResultProcessing {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val registrationViewModel: RegistrationViewModel by viewModels(
        factoryProducer = { RegistrationViewModelFactory(context = requireContext()) }
    )

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    private val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                registrationViewModel.stateScreen.collect{ result ->
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                registrationViewModel.isSetupCityText.collect{
                    setupCityText()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        val toolbarSettings = ToolbarSettings(toolbar = binding.layoutToolbarMainRegistration.toolbarMain)

        navControllerMain = findNavController()

        toolbarSettings.installToolbarMain(icon = R.drawable.ic_back) {
            navControllerMain.navigateUp()
        }

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

            registrationViewModel.setIsShownToast()
            registrationViewModel.register(
                isNetworkStatus = isNetworkStatus,
                userInfoModel = userInfoModel
            )
        }

        layoutPendingResultRegistration.bTryAgain.setOnClickListener {
            registrationViewModel.tryAgain(isNetworkStatus = isNetworkStatus){
                updateUI(flag = FLAG_SUCCESS_RESULT)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","RegistrationFragment onSuccessResultListener")
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
                TYPE_CREATE_USER -> {
                    Log.i("TAG","TYPE_CREATE_USER")
                    registrationViewModel.getUserId(isNetworkStatus = isNetworkStatus)
                }
                TYPE_GET_USER_ID -> {
                    Log.i("TAG","TYPE_GET_USER_ID")
                    val resultGetUserId = listRequests.find { it.type == TYPE_GET_USER_ID }?.result!!.asSuccess()!!

                    val responseGetUserId = resultGetUserId.data as ResponseValueModel<*>

                    val userId = responseGetUserId.value as Int

                    registrationViewModel.saveNumberPhone { numberPhone ->
                        sharedPreferences.edit().putString(KEY_USER_NUMBER_PHONE, numberPhone).apply()
                    }

                    sharedPreferences.edit().apply {
                        putBoolean(KEY_IS_INIT, false).apply()
                        putInt(KEY_USER_ID, userId).apply()
                    }

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
        registrationViewModel.onError(
            exception = exception,
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
        Log.i("TAG","RegistrationFragment onLoadingResultListener")
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultRegistration){
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

    private fun setupCityText() {

        val listCity = listOf(
            getString(R.string.cheboksary),
            getString(R.string.novocheboksarsk)
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        binding.actvCity.setAdapter(adapter)
    }

}