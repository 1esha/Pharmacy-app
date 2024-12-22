package com.example.pharmacyapp.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.domain.DataEntryError
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.IdentificationError
import com.example.domain.Network
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
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
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.EditViewModel
import com.example.pharmacyapp.tabs.viewmodels.AuthorizedUserViewModel


class EditFragment : Fragment(), ProfileResult {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val editViewModel: EditViewModel by viewModels()

    private val authorizedUserViewModel: AuthorizedUserViewModel by activityViewModels()

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

        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val navControllerMain = findNavController()

        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        val toolbarSettings = ToolbarSettings(toolbar = binding.layoutToolbarMainEdit.toolbarMain)

        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())

        val network = Network()

        toolbarSettings.installToolbarMain(R.drawable.ic_back){
            navControllerMain.navigateUp()
        }

        if (!isShown){
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    with(editViewModel){
                        setResult(result = PendingResult())
                        getUserById(userId = userId)
                    }
                },
                disconnectionListener = {
                    editViewModel.setResult(result = ErrorResult(exception = Exception()), errorType = DisconnectionError())
                }
            )

            editViewModel.setIsShown(isShown = true)
        }

        bEdit.setOnClickListener {
            network.checkNetworkStatus(
                isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext()),
                connectionListener = {
                    editViewModel.setIsShownSuccessResultEditUser(isShown = false)
                    editViewModel.setResult(result = PendingResult())
                    val userInfoModel = UserInfoModel(
                        etFirstNameForEdit.text.toString(),
                        etLastNameForEdit.text.toString(),
                        etEmailForEdit.text.toString(),
                        etPhoneNumberForEdit.text.toString(),
                        etPasswordForEdit.text.toString(),
                        actvCityForEdit.text.toString(),
                    )
                    editViewModel.editUser(userInfoModel = userInfoModel, userId = userId)
                },
                disconnectionListener = {
                    editViewModel.setResult(result = ErrorResult(exception = Exception()), errorType = DisconnectionError())
                    getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
                }
            )

        }

        bCancel.setOnClickListener {
            navControllerMain.popBackStack()
        }

        layoutPendingResultEdit.bTryAgain.setOnClickListener {
            network.checkNetworkStatus(
                isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext()),
                connectionListener = {
                    with(editViewModel){
                        setResult(result = PendingResult())
                        getUserById(userId = userId)
                    }
                },
                disconnectionListener = {
                    editViewModel.setResult(result = ErrorResult(exception = Exception()), errorType = DisconnectionError())
                    getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
                }
            )
        }

        editViewModel.resultGetUserById.observe(viewLifecycleOwner){ result ->
            Log.i("TAG","EditFragment current resultGetUserById = $result")
            val isShownSuccessResult = editViewModel.isShownSuccessResultGetUserById.value?:throw NullPointerException("EditFragment isShownSuccessResultGetUserById = null")
            when (result) {
                is PendingResult -> { onPendingResult() }
                is SuccessResult -> {
                    if (!isShownSuccessResult){
                        val value = result.value?: throw NullPointerException("EditFragment result.value = null")
                        onSuccessResultListener(userId = userId, value = value, type = TYPE_GET_USER_BY_ID)
                    }
                    else{
                        updateUI(
                            isVisible = false,
                            isProgressBar = false,
                            isButton = false,
                            isMessage = false,
                            message = null
                        )
                        setupCityText(autoCompleteTextView = binding.actvCityForEdit)
                    }
                    editViewModel.setIsShownSuccessResultGetUserById(isShown = true)
                }
                is ErrorResult -> {
                    val errorType = editViewModel.errorType.value
                    val message = when(errorType){
                        is DisconnectionError -> getString(R.string.check_your_internet_connection)
                        is IdentificationError -> getString(R.string.error_in_getting_the_id)
                        is DataEntryError -> getString(R.string.enter_the_data)
                        else -> getString(R.string.error)
                    }
                    onErrorResultListener(exception = result.exception, message = message)

                }
            }

        }

        editViewModel.resultEditUser.observe(viewLifecycleOwner){ result ->
            Log.i("TAG","EditFragment current resultEditUser = $result")
            val isShownSuccessResult = editViewModel.isShownSuccessResultEditUser.value?:throw NullPointerException("EditFragment isShownSuccessResultEditUser = null")
             when(result){

                 is PendingResult -> { onPendingResult() }

                 is SuccessResult -> {
                     updateUI(
                         isVisible = false,
                         isProgressBar = false,
                         isButton = false,
                         isMessage = false,
                         message = null
                     )
                     setupCityText(autoCompleteTextView = binding.actvCityForEdit)
                     if (!isShownSuccessResult){
                         val value = result.value
                         onSuccessResultListener(userId = userId, value = value, type = TYPE_EDIT_USER)

                     }
                     editViewModel.setIsShownSuccessResultEditUser(isShown = true)
                 }

                 is ErrorResult -> {
                     val errorType = editViewModel.errorType.value
                     val message = when(errorType){
                         is DisconnectionError -> getString(R.string.check_your_internet_connection)
                         is IdentificationError -> getString(R.string.error_in_getting_the_id)
                         is DataEntryError -> getString(R.string.enter_the_data)
                         else -> getString(R.string.error)
                     }
                     onErrorResultListener(exception = result.exception, message = message)
                 }
             }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupCityText(autoCompleteTextView: AutoCompleteTextView) {
        Log.i("TAG", "setupCityText")

        val listCity = listOf(
            getString(R.string.cheboksary),
            getString(R.string.novocheboksarsk)
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        autoCompleteTextView.setAdapter(adapter)
    }


    override fun onErrorResultListener(exception: Exception, message: String) {
        updateUI(
            isVisible = true,
            isProgressBar = false,
            isButton = true,
            isMessage = true,
            message = message
        )
    }

    override fun onPendingResult() {
        editViewModel.clearErrorType()
        updateUI(
            isVisible = true,
            isProgressBar = true,
            isButton = false,
            isMessage = false,
            message = null
        )
    }

    override fun <T> onSuccessResultListener(userId: Int, value: T, type: String?) = with(binding) {
        when(type?: TYPE_OTHER){
            TYPE_GET_USER_BY_ID -> {
                Log.i("TAG","EditFragment onSuccessResultListener")
                val responseValueModel = value as ResponseValueModel<*>
                val status = responseValueModel.responseModel.status
                val message = responseValueModel.responseModel.message
                if (status in 200..299){
                    updateUI(
                        isVisible = false,
                        isProgressBar = false,
                        isButton = false,
                        isMessage = false,
                        message = null
                    )

                    val userModel = responseValueModel.value as UserModel? ?: throw NullPointerException("EditFragment userModel = null")
                    etFirstNameForEdit.setText(userModel.userInfoModel.firstName)
                    etLastNameForEdit.setText(userModel.userInfoModel.lastName)
                    etEmailForEdit.setText(userModel.userInfoModel.email)
                    etPhoneNumberForEdit.setText(userModel.userInfoModel.phoneNumber)
                    etPasswordForEdit.setText(userModel.userInfoModel.userPassword)
                    actvCityForEdit.setText(userModel.userInfoModel.city)

                    setupCityText(autoCompleteTextView = actvCityForEdit)

                }
                else{
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            TYPE_EDIT_USER -> {
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
            TYPE_DELETE_USER -> {}
            else -> {}
        }
    }

    private fun updateUI(
        isVisible: Boolean,
        isProgressBar: Boolean,
        isButton: Boolean,
        isMessage: Boolean,
        message: String?
    ) = with(binding.layoutPendingResultEdit) {
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