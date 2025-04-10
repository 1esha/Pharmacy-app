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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_CITY
import com.example.pharmacyapp.KEY_FIRST_NAME
import com.example.pharmacyapp.KEY_LAST_NAME
import com.example.pharmacyapp.KEY_RESULT_FROM_EDIT_USER
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.KEY_USER_NUMBER_PHONE
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_DELETE_USER
import com.example.pharmacyapp.TYPE_EDIT_USER
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.ToolbarSettings
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentEditBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.EditViewModel
import com.example.pharmacyapp.main.viewmodels.factories.EditViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class EditFragment : Fragment(), ResultProcessing{

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val editViewModel: EditViewModel by viewModels(
        factoryProducer = { EditViewModelFactory() }
    )

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        editViewModel.initValues(userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER))

        editViewModel.sendingRequests(isNetworkStatus = isNetworkStatus)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                editViewModel.stateScreen.collect{ result ->
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
                editViewModel.userModel.collect{ userModel ->
                    if (userModel != null) installUI(userModel = userModel)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerMain = findNavController()

        val toolbarSettings = ToolbarSettings(toolbar = layoutToolbarMainEdit.toolbarMain)

        val dialogDeleteAccountListener = DialogInterface.OnClickListener { dialogInterface, currentButton ->
            when(currentButton){
                DialogInterface.BUTTON_POSITIVE -> {
                    editViewModel.deleteUser(isNetworkStatus = isNetworkStatus)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialogInterface.dismiss()
                }
            }
        }

        val dialogDeleteAccount = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.do_you_really_want_to_delete_your_account)
            .setMessage(R.string.after_deleting_the_account_all_data_about_it_will_be_deleted)
            .setPositiveButton(R.string.delete, dialogDeleteAccountListener)
            .setNegativeButton(R.string.cancel, dialogDeleteAccountListener)
            .create()

        toolbarSettings.installToolbarMain(R.drawable.ic_back){
            navControllerMain.navigateUp()
        }

        bEdit.setOnClickListener {
            onEditUser()
        }

        bDeleteAccount.setOnClickListener {
            dialogDeleteAccount.show()
        }

        layoutPendingResultEdit.bTryAgain.setOnClickListener {
            editViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        bCancel.setOnClickListener {
            navControllerMain.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onErrorResultListener(exception: Exception) {
        Log.i("TAG","EditFragment onErrorResultListener")
        editViewModel.onError(
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
        Log.i("TAG","EditFragment onLoadingResultListener")

        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","EditFragment onSuccessResultListener")
        try {
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
                TYPE_GET_USER_BY_ID -> {
                    Log.i("TAG","fullType = TYPE_GET_USER_BY_ID")
                    val result = listRequests.find { it.type == TYPE_GET_USER_BY_ID }?.result!!.asSuccess()!!

                    val responseValueModel = result.data as ResponseValueModel<*>

                    val userModel = responseValueModel.value as UserModel

                    editViewModel.fillData(userModel = userModel)
                }
                TYPE_EDIT_USER -> {
                    Log.i("TAG","fullType = TYPE_EDIT_USER")

                    editViewModel.onSuccessfullyEdited { userModel ->

                        sharedPreferences.edit().putString(KEY_USER_NUMBER_PHONE,userModel.userInfoModel.phoneNumber).apply()

                        val bundle = Bundle().apply {
                            putString(KEY_FIRST_NAME,userModel.userInfoModel.firstName)
                            putString(KEY_LAST_NAME,userModel.userInfoModel.lastName)
                            putString(KEY_CITY,userModel.userInfoModel.city)
                        }

                        getSupportActivity().setFragmentResult(KEY_RESULT_FROM_EDIT_USER,bundle)
                        getSupportActivity().showToast(getString(R.string.the_data_has_been_successfully_edited))
                    }

                }
                TYPE_DELETE_USER -> {
                    Log.i("TAG","fullType = TYPE_DELETE_USER")
                    sharedPreferences.edit().putInt(KEY_USER_ID, UNAUTHORIZED_USER).apply()
                    sharedPreferences.edit().putString(KEY_USER_NUMBER_PHONE, null).apply()
                    navControllerMain.popBackStack()
                    getSupportActivity().showToast(getString(R.string.account_deleted))
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
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

    private fun onEditUser() = with(binding){
        val userInfoModel = UserInfoModel(
            firstName = etFirstNameForEdit.text.toString(),
            lastName = etLastNameForEdit.text.toString(),
            email = etEmailForEdit.text.toString(),
            phoneNumber = etPhoneNumberForEdit.text.toString(),
            userPassword = etPasswordForEdit.text.toString(),
            city = actvCityForEdit.text.toString(),
        )
        editViewModel.onEditUser(
            isNetworkStatus = isNetworkStatus,
            userInfoModel = userInfoModel
        )
    }

    private fun installUI(userModel: UserModel) = with(binding){

        editViewModel.installUI {
            etFirstNameForEdit.setText(userModel.userInfoModel.firstName)
            etLastNameForEdit.setText(userModel.userInfoModel.lastName)
            etEmailForEdit.setText(userModel.userInfoModel.email)
            etPhoneNumberForEdit.setText(userModel.userInfoModel.phoneNumber)
            etPasswordForEdit.setText(userModel.userInfoModel.userPassword)
            actvCityForEdit.setText(userModel.userInfoModel.city)
        }
        setupCityText()
    }

    private fun setupCityText() {

        val listCity = listOf(
            getString(R.string.cheboksary),
            getString(R.string.novocheboksarsk)
        )

        val adapter = ArrayAdapter(requireContext(), R.layout.item_city, listCity)
        binding.actvCityForEdit.setAdapter(adapter)
    }
}