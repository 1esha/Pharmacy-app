package com.example.pharmacyapp.tabs.profile

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_CITY
import com.example.pharmacyapp.KEY_FIRST_NAME
import com.example.pharmacyapp.KEY_LAST_NAME
import com.example.pharmacyapp.KEY_RESULT_USER_INFO
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_DELETE_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.TYPE_OTHER
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentAuthorizedUserBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.AuthorizedUserViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.factories.AuthorizedUserViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class AuthorizedUserFragment : Fragment(), ProfileResult {

    private var _binding: FragmentAuthorizedUserBinding? = null
    private val binding get() = _binding!!

    private val authorizedUserViewModel: AuthorizedUserViewModel by viewModels(
        factoryProducer = { AuthorizedUserViewModelFactory(context = requireContext()) },
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerProfile: NavController

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getSupportActivity().setFragmentResultListener(KEY_RESULT_USER_INFO) { requestKey, bundle ->
            with(bundle) {
                val firstName =  getString(KEY_FIRST_NAME) ?:
                throw NullPointerException("AuthorizedUserFragment firstName = null")

                val lastName = getString(KEY_LAST_NAME) ?:
                throw NullPointerException("AuthorizedUserFragment lastName = null")

                val city = getString(KEY_CITY) ?:
                throw NullPointerException("AuthorizedUserFragment city = null")

                authorizedUserViewModel.updateUserModel(
                    firstName = firstName,
                    lastName = lastName,
                    city = city
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorizedUserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        navControllerProfile = findNavController()

        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        val isShown: Boolean = authorizedUserViewModel.isShown.value?:throw NullPointerException("AuthorizedUserFragment isShown = null")

        val navControllerMain = getSupportActivity().getNavControllerMain()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.account)){})
        toolbarViewModel.clearMenu()

        val dialogListener = DialogInterface.OnClickListener { dialogInterface, currentButton ->
            when(currentButton){
                DialogInterface.BUTTON_POSITIVE -> {
                    onSuccessfulEvent(type = TYPE_DELETE_ALL_FAVORITES) {
                        authorizedUserViewModel.deleteAllFavorites()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialogInterface.dismiss()
                }
            }
        }

        val dialogExit = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.do_you_really_want_to_get_out)
            .setMessage(R.string.if_you_log_out_of_your_account_all_product_information_from_the_favorites_section_will_be_deleted)
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
        }

        layoutPendingResultAuthorizedUser.bTryAgain.setOnClickListener {
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

        authorizedUserViewModel.mediatorAuthorizedUser.observe(viewLifecycleOwner) { mediatorResult ->
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
                    val errorType = authorizedUserViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }

        authorizedUserViewModel.userModelLivedata.observe(viewLifecycleOwner){ userModel ->

            binding.tvFullName.text = userModel.userInfoModel.firstName + " " + userModel.userInfoModel.lastName
            binding.tvCurrentCity.text = userModel.userInfoModel.city
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(userId: Int, value: T, type: String?) {

        when(type?: TYPE_OTHER){
            TYPE_GET_USER_BY_ID -> {
                Log.i("TAG","AuthorizedUserFragment onSuccessResultListener TYPE_GET_USER_BY_ID")
                val isShown: Boolean = authorizedUserViewModel.isShown.value?:throw NullPointerException("AuthorizedUserFragment isShown = null")
                if (!isShown) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message
                    if (status in 200..299) {

                        val userModel = responseValueModel.value as UserModel ?: throw NullPointerException("AuthorizedUserFragment userModel = null")

                        authorizedUserViewModel.setUserModel(userModel = userModel)
                        updateUI(flag = FLAG_SUCCESS_RESULT)
                    } else {
                        authorizedUserViewModel.setResultGetUserById(ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }
                authorizedUserViewModel.setIsShown(isShown = true)
            }
            TYPE_DELETE_ALL_FAVORITES -> {
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {
                    sharedPreferences.edit().putInt(KEY_USER_ID, UNAUTHORIZED_USER).apply()
                    navControllerProfile.navigate(R.id.unauthorizedUserFragment,null, navOptions {
                        popUpTo(R.id.authorizedUserFragment) {
                            inclusive = true
                        }
                    })
                }

            }
        }

    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        updateUI(flag = FLAG_ERROR_RESULT)

    }

    override fun onPendingResultListener() {
        Log.i("TAG", "AuthorizedUserFragment onPendingResult")
        authorizedUserViewModel.clearErrorType()
        updateUI(flag = FLAG_PENDING_RESULT)

    }

    override fun onSuccessfulEvent(type: String, exception: Exception?,onSuccessfulEventListener:() -> Unit){
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

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultAuthorizedUser) {
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