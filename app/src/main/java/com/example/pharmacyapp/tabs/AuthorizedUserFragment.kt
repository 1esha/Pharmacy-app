package com.example.pharmacyapp.tabs

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.IdentificationError
import com.example.domain.Network
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentAuthorizedUserBinding
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.tabs.viewmodels.AuthorizedUserViewModel


class AuthorizedUserFragment : Fragment(), ProfileResult {

    private var _binding: FragmentAuthorizedUserBinding? = null
    private val binding get() = _binding!!

    private val authorizedUserViewModel: AuthorizedUserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorizedUserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        val isShown: Boolean = authorizedUserViewModel.isShown.value?:throw NullPointerException("AuthorizedUserFragment isShown = null")

        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val navControllerMain = getSupportActivity().getNavControllerMain()

        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())

        val network = Network()

        if (!isShown){
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    with(authorizedUserViewModel){
                        setResult(result = PendingResult())
                        getUserById(
                            userId = userId
                        )
                    }
                },
                disconnectionListener = {
                    authorizedUserViewModel.setResult(
                        result = ErrorResult(exception = Exception()),
                        errorType = DisconnectionError())
                }
            )
            authorizedUserViewModel.setIsShown(isShown = true)
        }

        authorizedPendingResult.bTryAgain.setOnClickListener {

            network.checkNetworkStatus(
                isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext()),
                connectionListener = {
                    with(authorizedUserViewModel){
                        setResult(result = PendingResult())
                        getUserById(userId = userId)
                    }
                },
                disconnectionListener = {
                    authorizedUserViewModel.setResult(result = ErrorResult(exception = Exception()), errorType = DisconnectionError())
                    getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
                }
            )
        }

        cardUserInfo.setOnClickListener {
            navControllerMain.navigate(R.id.editFragment)
        }

        authorizedUserViewModel.result.observe(viewLifecycleOwner) { result ->
            Log.i("TAG", "AuthorizedUserFragment current result = $result")
            when (result) {
                is PendingResult -> {
                    onPendingResult()
                }

                is SuccessResult -> {
                    val value = result.value?: throw NullPointerException("AuthorizedUserFragment result.value = null")
                    onSuccessResultListener(userId = userId, value = value)
                }

                is ErrorResult -> {
                    val errorType = authorizedUserViewModel.errorType.value
                    val message = when(errorType){
                        is DisconnectionError -> getString(R.string.check_your_internet_connection)
                        is IdentificationError -> getString(R.string.error_in_getting_the_id)
                        else -> getString(R.string.error)
                    }
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }

        authorizedUserViewModel.userModelLivedata.observe(viewLifecycleOwner){ userModel ->
            updateUI(userModel = userModel)
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
        if (status in 200..299) {
            updatePendingResultUI(
                isVisible = false,
                isProgressBar = false,
                isButton = false,
                isMessage = false,
                message = null
            )
            val userModel = responseValueModel.value ?: throw NullPointerException("AuthorizedUserFragment userModel = null")
            updateUI(userModel = userModel as UserModel)
        } else {
            if (message != null) getSupportActivity().showToast(message = message)
        }
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        updatePendingResultUI(
            isVisible = true,
            isProgressBar = false,
            isButton = true,
            isMessage = true,
            message = message
        )
    }

    override fun onPendingResult() {
        Log.i("TAG", "AuthorizedUserFragment onPendingResult")
        authorizedUserViewModel.clearErrorType()
        updatePendingResultUI(
            isVisible = true,
            isProgressBar = true,
            isButton = false,
            isMessage = false,
            message = null
        )
    }

    private fun updateUI(userModel: UserModel){
        binding.tvFullName.text = userModel.userInfoModel.firstName + " " + userModel.userInfoModel.lastName
        binding.tvCurrentCity.text = userModel.userInfoModel.city
    }

    private fun updatePendingResultUI(
        isVisible: Boolean,
        isProgressBar: Boolean,
        isButton: Boolean,
        isMessage: Boolean,
        message: String?
    ) = with(binding.authorizedPendingResult) {
        if (isVisible){
            root.visibility = View.VISIBLE
        } else{
            root.visibility = View.GONE
        }

        if (isProgressBar){
            progressBar.visibility = View.VISIBLE
        } else{
            progressBar.visibility = View.GONE
        }

        if (isButton){
            bTryAgain.visibility = View.VISIBLE
        } else{
            bTryAgain.visibility = View.GONE
        }

        if (isMessage){
            tvErrorMessage.visibility = View.VISIBLE
            tvErrorMessage.text = message
        } else{
            tvErrorMessage.visibility = View.GONE
        }
    }

}