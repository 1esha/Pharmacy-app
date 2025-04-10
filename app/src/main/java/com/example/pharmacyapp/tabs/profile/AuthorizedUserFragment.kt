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
import com.example.pharmacyapp.TYPE_DELETE_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentAuthorizedUserBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.AuthorizedUserViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.factories.AuthorizedUserViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch


class AuthorizedUserFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentAuthorizedUserBinding? = null
    private val binding get() = _binding!!

    private val authorizedUserViewModel: AuthorizedUserViewModel by viewModels(
        factoryProducer = { AuthorizedUserViewModelFactory(context = requireContext()) },
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerProfile: NavController

    private lateinit var navControllerMain: NavController

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        authorizedUserViewModel.initValues(userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER))

        getSupportActivity().setFragmentResultListener(KEY_RESULT_FROM_EDIT_USER) { requestKey, bundle ->
            with(bundle) {
                authorizedUserViewModel.listenResultFromEditUser(
                    firstName = getString(KEY_FIRST_NAME),
                    lastName = getString(KEY_LAST_NAME) ,
                    city = getString(KEY_CITY)
                )
            }
        }

        authorizedUserViewModel.sendingRequests(isNetworkStatus = isNetworkStatus)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                authorizedUserViewModel.stateScreen.collect{ result ->
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
                authorizedUserViewModel.userModel.collect{ userModel ->
                    if (userModel != null){
                        with(binding){
                            val fullName = "${userModel.userInfoModel.firstName} ${userModel.userInfoModel.lastName}"
                            tvFullName.text = fullName
                            tvCurrentCity.text = userModel.userInfoModel.city
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
        _binding = FragmentAuthorizedUserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.account)){})
        toolbarViewModel.clearMenu()

        navControllerProfile = findNavController()

        navControllerMain = getSupportActivity().getNavControllerMain()

        tvVersionName.text = getSupportActivity().getVersionName()

        // При выходе из аккаунта отчистится список избранного и номер телефона
        val dialogListener = DialogInterface.OnClickListener { dialogInterface, currentButton ->
            when(currentButton){
                DialogInterface.BUTTON_POSITIVE -> {

                    authorizedUserViewModel.deleteAllFavorites()
                    sharedPreferences.edit().putString(KEY_USER_NUMBER_PHONE, null).apply()
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

        layoutPendingResultAuthorizedUser.bTryAgain.setOnClickListener {
            authorizedUserViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        layoutFavorites.setOnClickListener {
            navControllerProfile.navigate(R.id.action_authorizedUserFragment_to_favoriteFragment)
        }

        layoutPharmacyAddresses.setOnClickListener {
            navControllerMain.navigate(R.id.mapFragment)
        }

        cardUserInfo.setOnClickListener {
            navControllerMain.navigate(R.id.editFragment)
        }

        bExit.setOnClickListener {
            dialogExit.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","AuthorizedUserFragment onSuccessResultListener")
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
                    val resultGetUserById = listRequests.find { it.type == TYPE_GET_USER_BY_ID }?.result!!.asSuccess()!!

                    val responseGetUserById = resultGetUserById.data as ResponseValueModel<*>

                    val userModel = responseGetUserById.value as UserModel

                    authorizedUserViewModel.fillData(userModel = userModel)
                }
                TYPE_DELETE_ALL_FAVORITES -> {
                    sharedPreferences.edit().putInt(KEY_USER_ID, UNAUTHORIZED_USER).apply()
                    navControllerProfile.navigate(
                        R.id.unauthorizedUserFragment,null, navOptions {
                        popUpTo(R.id.authorizedUserFragment) {
                            inclusive = true
                        }
                        }
                    )
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: java.lang.Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        Log.i("TAG", "AuthorizedUserFragment onErrorResultListener")
        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        Log.i("TAG", "AuthorizedUserFragment onLoadingResultListener")
        updateUI(flag = FLAG_PENDING_RESULT)
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