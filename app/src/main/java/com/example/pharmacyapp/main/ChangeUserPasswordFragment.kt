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
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.models.RequestModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_CHANGE_USER_PASSWORD
import com.example.pharmacyapp.ToolbarSettings
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentChangeUserPasswordBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ChangeUserPasswordViewModel
import com.example.pharmacyapp.main.viewmodels.factories.ChangeUserPasswordViewModelFactory
import kotlinx.coroutines.launch


class ChangeUserPasswordFragment : Fragment(), ResultProcessing{

    private var _binding: FragmentChangeUserPasswordBinding? = null
    private val binding get() = _binding!!

    private val changeUserPasswordViewModel: ChangeUserPasswordViewModel by viewModels(
        factoryProducer = { ChangeUserPasswordViewModelFactory(context = requireContext()) }
    )

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        changeUserPasswordViewModel.initValues(
            userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                changeUserPasswordViewModel.stateScreen.collect{ result ->
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
        _binding = FragmentChangeUserPasswordBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerMain = findNavController()

        val toolbarSettings = ToolbarSettings(toolbar = layoutToolbarMainChangeUserPassword.toolbarMain)

        toolbarSettings.installToolbarMain(
            icon = R.drawable.ic_back,
            title = getString(R.string.password_change)
        ){
            navControllerMain.navigateUp()
        }

        layoutPendingResultChangeUserPassword.bTryAgain.setOnClickListener {
            changeUserPasswordViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        bChangeUserPassword.setOnClickListener {
            changeUserPasswordViewModel.onChangeUserPassword(
                isNetworkStatus = isNetworkStatus,
                newPassword = etNewPassword.text.toString(),
                currentPassword = etCurrentPassword.text.toString(),
                repeatCurrentPassword = etRepeatCurrentPassword.text.toString()
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onErrorResultListener(exception: Exception) {
        Log.i("TAG","ChangeUserPasswordFragment onErrorResultListener")

        changeUserPasswordViewModel.onError(
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
        Log.i("TAG","ChangeUserPasswordFragment onLoadingResultListener")

        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","ChangeUserPasswordFragment onSuccessResultListener")
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
                TYPE_CHANGE_USER_PASSWORD -> {
                    getSupportActivity().showToast(getString(R.string.successful_password_change))
                    navControllerMain.popBackStack()
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultChangeUserPassword) {
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
}