package com.example.pharmacyapp.tabs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.TYPE_OTHER
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentAuthorizedUserBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.tabs.viewmodels.AuthorizedUserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


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

        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        val isShown: Boolean = authorizedUserViewModel.isShown.value?:throw NullPointerException("AuthorizedUserFragment isShown = null")

        val navControllerMain = getSupportActivity().getNavControllerMain()

        val dialogListener = DialogInterface.OnClickListener { dialogInterface, currentButton ->
            when(currentButton){
                DialogInterface.BUTTON_POSITIVE -> {
                    sharedPreferences.edit().putInt(KEY_USER_ID, UNAUTHORIZED_USER).apply()
                    findNavController().navigate(R.id.unauthorizedUserFragment)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialogInterface.dismiss()
                }
            }
        }

        val dialogExit = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.log_out_of_your_account)
            .setPositiveButton(R.string.log_out, dialogListener)
            .setNegativeButton(R.string.cancel, dialogListener)
            .create()

        if (!isShown){
            onSuccessfulEvent(type = TYPE_GET_USER_BY_ID) {
                with(authorizedUserViewModel){
                    getUserById(
                        userId = userId
                    )
                }
            }
            authorizedUserViewModel.setIsShown(isShown = true)
        }

        authorizedPendingResult.bTryAgain.setOnClickListener {
            onSuccessfulEvent(type = TYPE_GET_USER_BY_ID) {
                with(authorizedUserViewModel){
                    getUserById(userId = userId)
                }
            }
        }

        cardUserInfo.setOnClickListener {
            navControllerMain.navigate(R.id.editFragment)
        }

        bExit.setOnClickListener {
            dialogExit.show()
        }

        authorizedUserViewModel.mediatorLiveData.observe(viewLifecycleOwner) { mediatorResult ->
            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResult()}
                is SuccessResult -> {
                    onSuccessResultListener(
                        userId = userId,
                        value = result.value,
                        type = type
                    )
                }
                is ErrorResult -> {
                    val errorType = authorizedUserViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
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

        when(type?: TYPE_OTHER){
            TYPE_GET_USER_BY_ID -> {
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
                    authorizedUserViewModel.setResultGetUserById(ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
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

    private fun onSuccessfulEvent(type: String, exception: Exception? = null,onSuccessfulEventListener:() -> Unit){
        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())
        val network = Network()

        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                when(type) {
                    TYPE_GET_USER_BY_ID -> authorizedUserViewModel.setResultGetUserById(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()
                when(type) {
                    TYPE_GET_USER_BY_ID -> authorizedUserViewModel.setResultGetUserById(result = ErrorResult(exception = currentException), errorType = errorType)
                }
                getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
            }
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