package com.example.pharmacyapp.tabs.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentFavoriteBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.profile.adapters.FavoriteAdapter
import com.example.pharmacyapp.tabs.profile.viewmodels.FavoriteViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.factories.FavoriteViewModelFactory
import java.lang.Exception

class FavoriteFragment : Fragment(), ProfileResult {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val favoriteViewModel: FavoriteViewModel by viewModels(
        factoryProducer = { FavoriteViewModelFactory(context = requireContext()) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isShownGetAllFavorites = favoriteViewModel.isShownGetAllFavorites

        if (!isShownGetAllFavorites) {
            onSuccessfulEvent(type = TYPE_GET_ALL_FAVORITES) {
                favoriteViewModel.getAllFavorites()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding){

        val navControllerProfile = findNavController()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.favourites),
            icon = R.drawable.ic_back
        ) {
            navControllerProfile.navigateUp()
        })
        toolbarViewModel.clearMenu()

        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        layoutPendingResultFavorite.bTryAgain.setOnClickListener {
            onSuccessfulEvent(type = TYPE_GET_ALL_FAVORITES) {
                with(favoriteViewModel) {
                    setIsShownGetAllFavorites(isShown = false)
                    getAllFavorites()
                }
            }
        }

        favoriteViewModel.resultGetAllFavorites.observe(viewLifecycleOwner) { result ->

            when(result){
                is PendingResult -> { onPendingResultListener() }
                is SuccessResult -> {
                    onSuccessResultListener(
                        userId = userId,
                        value = result.value,
                        type = TYPE_GET_ALL_FAVORITES)
                }
                is ErrorResult -> {
                    val errorType = favoriteViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }

        favoriteViewModel.listAllFavorite.observe(viewLifecycleOwner) { listAllFavorite ->
            val favoriteAdapter = FavoriteAdapter(listItems = listAllFavorite)

            rvFavorite.adapter = favoriteAdapter
            rvFavorite.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(userId: Int, value: T, type: String?) {

        when(type) {
            TYPE_GET_ALL_FAVORITES -> {
                Log.i("TAG","FavoriteFragment onSuccessResultListener TYPE_GET_ALL_FAVORITES")
                val isShownGetAllFavorites = favoriteViewModel.isShownGetAllFavorites

                if (!isShownGetAllFavorites) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val responseModel = responseValueModel.responseModel
                    val status = responseModel.status
                    val message = responseModel.message

                    if (status in 200..299) {
                        val listAllFavorites = responseValueModel.value as List<*>
                        favoriteViewModel.setListAllFavorites(list = listAllFavorites)
                        updateUI(flag = FLAG_SUCCESS_RESULT)
                    }
                    else {
                        favoriteViewModel.setResultGetAllFavorite(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                favoriteViewModel.setIsShownGetAllFavorites(isShown = true)
            }
        }

    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG","FavoriteFragment onErrorResultListener")
        updateUI(flag = FLAG_ERROR_RESULT, messageError = message)
        favoriteViewModel.setIsShownGetAllFavorites(isShown = true)
    }

    override fun onPendingResultListener() {
        Log.i("TAG","FavoriteFragment onPendingResultListener")
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun onSuccessfulEvent(
        type: String,
        exception: Exception?,
        onSuccessfulEventListener: () -> Unit
    ) {
        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())
        val network = Network()

        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {

                when(type) {
                    TYPE_GET_ALL_FAVORITES -> favoriteViewModel.setResultGetAllFavorite(result = PendingResult())
                }

                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()

                when(type) {
                    TYPE_GET_ALL_FAVORITES -> favoriteViewModel.setResultGetAllFavorite(result = ErrorResult(exception = currentException), errorType = errorType)
                }

                getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultFavorite){
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