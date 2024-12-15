package com.example.pharmacyapp.tabs

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.domain.ErrorResult
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
import java.lang.Exception

class AuthorizedUserFragment : Fragment(), ProfileResult<ResponseValueModel<UserModel>> {

    private var _binding: FragmentAuthorizedUserBinding? = null
    private val binding get() = _binding!!

    override var isShow: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorizedUserBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        val authorizedUserViewModel: AuthorizedUserViewModel by viewModels()

        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())

        if (isNetworkStatus){
            val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

            authorizedUserViewModel.getUserById(
                userId = userId,
                getStringById = { resources.getString(R.string.error_in_getting_the_id) }
            )

            authorizedUserViewModel.result.observe(viewLifecycleOwner){ result ->
                when (result) {
                    is PendingResult -> { onPendingResult() }
                    is SuccessResult -> {
                        val value = result.value?: throw NullPointerException("AuthorizedUserFragment result.value = null")
                        onSuccessResultListener(userId = userId, value = value)
                    }

                    is ErrorResult -> {
                        onErrorResultListener(exception = result.exception)
                    }
                }
            }

        }
        else{
            binding.authorizedPendingResult.root.visibility = View.VISIBLE
            binding.authorizedPendingResult.bTryAgain.visibility = View.VISIBLE
            getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
        }

        authorizedPendingResult.bTryAgain.setOnClickListener {
            onViewCreated(view, savedInstanceState)
        }

        cardUserInfo.setOnClickListener {

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSuccessResultListener(userId: Int, value: ResponseValueModel<UserModel>) {
        val status = value.responseModel.status
        val message = value.responseModel.message
        if (status in 200..299){
            binding.authorizedPendingResult.root.visibility = View.GONE
            val userModel = value.value?: throw NullPointerException("AuthorizedUserFragment userModel = null")
            binding.tvFullName.text = userModel.userInfoModel.firstName+" "+userModel.userInfoModel.lastName
            binding.tvCurrentCity.text = userModel.userInfoModel.city
        }
        else{
            if (message != null) getSupportActivity().showToast(message = message)
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        binding.authorizedPendingResult.bTryAgain.visibility = View.VISIBLE
        getSupportActivity().showToast(message = resources.getString(R.string.error))
    }

    override fun onPendingResult() {
        Log.i("TAG","AuthorizedUserFragment onPendingResult")
        binding.authorizedPendingResult.root.visibility = View.VISIBLE
    }

}