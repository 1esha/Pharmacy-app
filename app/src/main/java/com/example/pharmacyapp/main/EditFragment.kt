package com.example.pharmacyapp.main

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
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
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_DELETE_USER
import com.example.pharmacyapp.TYPE_EDIT_USER
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.TYPE_OTHER
import com.example.pharmacyapp.ToolbarSettings
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentEditBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.EditViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.AuthorizedUserViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class EditFragment : Fragment(), ProfileResult {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val editViewModel: EditViewModel by viewModels()

    private val authorizedUserViewModel: AuthorizedUserViewModel by activityViewModels()

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        val isShown: Boolean = editViewModel.isShown.value?:throw NullPointerException("EditFragment isShown = null")

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        navControllerMain = findNavController()

        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        val toolbarSettings = ToolbarSettings(toolbar = layoutToolbarMainEdit.toolbarMain)

        val dialogListener = DialogInterface.OnClickListener { dialogInterface, currentButton ->
            when(currentButton){
                DialogInterface.BUTTON_POSITIVE -> {
                    onSuccessfulEvent(type = TYPE_DELETE_USER) {
                        with(editViewModel){
                            deleteUser(userId = userId)
                        }
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialogInterface.dismiss()
                }
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.do_you_really_want_to_delete_your_account)
            .setMessage(R.string.after_deleting_the_account_all_data_about_it_will_be_deleted)
            .setPositiveButton(R.string.delete, dialogListener)
            .setNegativeButton(R.string.cancel, dialogListener)
            .create()

        toolbarSettings.installToolbarMain(R.drawable.ic_back){
            navControllerMain.navigateUp()
        }

        if (!isShown){
            onSuccessfulEvent(type = TYPE_GET_USER_BY_ID) {
                with(editViewModel){
                    getUserById(userId = userId)
                }
            }

            editViewModel.setIsShown(isShown = true)
        }

        bEdit.setOnClickListener {
            onSuccessfulEvent(type = TYPE_EDIT_USER) {
                with(editViewModel){
                    setIsShownSuccessResultEditUser(isShown = false)
                    val userInfoModel = UserInfoModel(
                        etFirstNameForEdit.text.toString(),
                        etLastNameForEdit.text.toString(),
                        etEmailForEdit.text.toString(),
                        etPhoneNumberForEdit.text.toString(),
                        etPasswordForEdit.text.toString(),
                        actvCityForEdit.text.toString(),
                    )
                    editUser(userInfoModel = userInfoModel, userId = userId)
                }

            }

        }

        bDeleteAccount.setOnClickListener {
            dialog.show()
        }

        layoutPendingResultEdit.bTryAgain.setOnClickListener {
            onSuccessfulEvent(type = TYPE_GET_USER_BY_ID) {
                with(editViewModel){
                    getUserById(userId = userId)
                }
            }
        }

        bCancel.setOnClickListener {
            navControllerMain.popBackStack()
        }

        editViewModel.mediatorLiveData.observe(viewLifecycleOwner) { mediatorResult ->
            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResultListener()}
                is SuccessResult -> {
                    onSuccessResultListener(
                        userId = userId,
                        value = result.value,
                        type = type
                    )
                }
                is ErrorResult -> {
                    val errorType = editViewModel.errorType.value
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

    private fun setupCityText() {

        val listCity = listOf(
            getString(R.string.cheboksary),
            getString(R.string.novocheboksarsk)
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        binding.actvCityForEdit.setAdapter(adapter)
    }


    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG","EditFragment onErrorResultListener")

        updateUI(flag = FLAG_ERROR_RESULT, messageError = message)
    }

    override fun onPendingResultListener() {
        Log.i("TAG","EditFragment onPendingResult")
        editViewModel.clearErrorType()

        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(userId: Int, value: T, type: String?): Unit = with(binding) {
        Log.i("TAG","EditFragment onSuccessResultListener")
        when(type?: TYPE_OTHER){
            TYPE_GET_USER_BY_ID -> {
                val isShownSuccessResult = editViewModel.isShownSuccessResultGetUserById.value?:throw NullPointerException("EditFragment onSuccessResultListener isShownSuccessResultGetUserById = null")
                val responseValueModel = value as ResponseValueModel<*>
                val status = responseValueModel.responseModel.status
                val message = responseValueModel.responseModel.message
                if (status in 200..299){

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                    if (!isShownSuccessResult){
                        val userModel = responseValueModel.value as UserModel? ?: throw NullPointerException("EditFragment userModel = null")
                        etFirstNameForEdit.setText(userModel.userInfoModel.firstName)
                        etLastNameForEdit.setText(userModel.userInfoModel.lastName)
                        etEmailForEdit.setText(userModel.userInfoModel.email)
                        etPhoneNumberForEdit.setText(userModel.userInfoModel.phoneNumber)
                        etPasswordForEdit.setText(userModel.userInfoModel.userPassword)
                        actvCityForEdit.setText(userModel.userInfoModel.city)
                    }
                    setupCityText()
                }
                else{
                    editViewModel.setResultGetUserById(ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }

                editViewModel.setIsShownSuccessResultGetUserById(isShown = true)

            }
            TYPE_EDIT_USER -> {
                val isShownSuccessResult = editViewModel.isShownSuccessResultEditUser.value?:throw NullPointerException("EditFragment onSuccessResultListener isShownSuccessResultEditUser = null")

                updateUI(flag = FLAG_SUCCESS_RESULT)
                setupCityText()
                if (!isShownSuccessResult){

                    val userModel = UserModel(
                        userId = userId,
                        userInfoModel = UserInfoModel(
                            firstName = etFirstNameForEdit.text.toString(),
                            lastName = etLastNameForEdit.text.toString(),
                            email = etEmailForEdit.text.toString(),
                            phoneNumber = etPhoneNumberForEdit.text.toString(),
                            userPassword = etPasswordForEdit.text.toString(),
                            city = actvCityForEdit.text.toString()
                        )
                    )

                    authorizedUserViewModel.setUserModel(userModel = userModel)
                    getSupportActivity().showToast(message = getString(R.string.the_data_has_been_successfully_edited))
                }
                editViewModel.setIsShownSuccessResultEditUser(isShown = true)

            }
            TYPE_DELETE_USER -> {
                sharedPreferences.edit().putInt(KEY_USER_ID, UNAUTHORIZED_USER).apply()
                navControllerMain.popBackStack()
            }
            else -> {}
        }
    }

    override fun onSuccessfulEvent(type: String, exception: Exception?,onSuccessfulEventListener:() -> Unit){
        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())
        val network = Network()

        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                when(type) {
                    TYPE_GET_USER_BY_ID -> editViewModel.setResultGetUserById(result = PendingResult())
                    TYPE_EDIT_USER -> editViewModel.setResultEditUser(result = PendingResult())
                    TYPE_DELETE_USER -> editViewModel.setResultDeleteUser(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()
                Log.i("TAG","errorType = ${editViewModel.errorType.value}")
                when(type) {
                    TYPE_GET_USER_BY_ID -> editViewModel.setResultGetUserById(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_EDIT_USER -> editViewModel.setResultEditUser(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_DELETE_USER -> editViewModel.setResultDeleteUser(result = ErrorResult(exception = currentException), errorType = errorType)
                }
                getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultEdit) {
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