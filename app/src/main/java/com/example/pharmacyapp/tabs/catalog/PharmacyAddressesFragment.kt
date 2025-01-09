package com.example.pharmacyapp.tabs.catalog

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.catalog.CatalogResult
import com.example.domain.models.PharmacyAddressesModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.MenuSettingsModel
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentPharmacyAddressesBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.PharmacyAddressesAdapter
import com.example.pharmacyapp.tabs.catalog.viewmodels.PharmacyAddressesViewModel
import java.lang.Exception

class PharmacyAddressesFragment : Fragment(), CatalogResult {

    private var _binding: FragmentPharmacyAddressesBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val pharmacyAddressesViewModel: PharmacyAddressesViewModel by viewModels()

    private lateinit var navControllerCatalog: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPharmacyAddressesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerCatalog = findNavController()

        pharmacyAddressesViewModel.counterSelectedItems.observe(viewLifecycleOwner) { counter ->
            toolbarViewModel.setToolbarSettings(toolbarSettingsModel = ToolbarSettingsModel(
                title = "Selected $counter",
                icon = R.drawable.ic_back
            ) {
                navControllerCatalog.navigateUp()
            })
        }

        toolbarViewModel.setMenuSettings(menuSettingsModel = MenuSettingsModel(
            menu = R.menu.menu_select_pharmacy_addresses
        ) {
            onClickMenuItem()
        })

        val isShown = pharmacyAddressesViewModel.isShown.value
            ?: throw NullPointerException("PharmacyAddressesFragment isShown = null")

        if (!isShown) {

            onSuccessfulEvent(type = TYPE_GET_PHARMACY_ADDRESSES) {
                with(pharmacyAddressesViewModel) {
                    getPharmacyAddresses()
                }
            }
        }

        layoutPendingResultPharmacyAddresses.bTryAgain.setOnClickListener {
            onSuccessfulEvent(type = TYPE_GET_PHARMACY_ADDRESSES) {
                with(pharmacyAddressesViewModel) {
                    getPharmacyAddresses()
                }
            }
        }

        pharmacyAddressesViewModel.result.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PendingResult -> {
                    onPendingResult()
                }

                is SuccessResult -> {
                    val value = result.value
                    onSuccessResultListener(
                        value = value,
                        type = TYPE_GET_PHARMACY_ADDRESSES
                    )
                }

                is ErrorResult -> {
                    val errorType = pharmacyAddressesViewModel.errorType.value
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

    override fun <T> onSuccessResultListener(value: T, type: String?) {
        Log.i("TAG", "PharmacyAddressesFragment onSuccessResultListener")
        val responseValueModel = value as ResponseValueModel<*>
        val responseModel = responseValueModel.responseModel
        val status = responseModel.status
        val message = responseModel.message

        if (status in 200..299) {
            updateUI(flag = FLAG_SUCCESS_RESULT)
            val listItems = responseValueModel.value as List<*>

            pharmacyAddressesViewModel.setInitPharmacyAddresses(list = listItems)

            val mutableListSelectedPharmacyAddresses =
                pharmacyAddressesViewModel.listSelectedPharmacyAddresses.value
                    ?: throw NullPointerException("PharmacyAddressesFragment mutableListSelectedPharmacyAddresses = null")

            val pharmacyAddressesAdapter =
                PharmacyAddressesAdapter(listItems = mutableListSelectedPharmacyAddresses) { position, isSelect ->

                    pharmacyAddressesViewModel.setPharmacyAddresses(
                        position = position,
                        isSelect = isSelect
                    )

                    updateCounter()

                    Log.i("TAG","mutableListSelectedPharmacyAddresses = $mutableListSelectedPharmacyAddresses")

                }

            with(binding) {
                rvPharmacyAddresses.adapter = pharmacyAddressesAdapter
                rvPharmacyAddresses.layoutManager = LinearLayoutManager(requireContext())
            }

        } else {
            pharmacyAddressesViewModel.setResult(
                result = ErrorResult(exception = Exception()),
                errorType = OtherError()
            )
            if (message != null) getSupportActivity().showToast(message = message)
        }
        pharmacyAddressesViewModel.setIsShown(isShown = true)
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG", "PharmacyAddressesFragment onErrorResultListener")
        updateUI(FLAG_ERROR_RESULT, messageError = message)

        pharmacyAddressesViewModel.setIsShown(isShown = true)
    }

    override fun onPendingResult() {
        Log.i("TAG", "PharmacyAddressesFragment onPendingResult")
        pharmacyAddressesViewModel.clearErrorType()
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
                when (type) {
                    TYPE_GET_PHARMACY_ADDRESSES -> pharmacyAddressesViewModel.setResult(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else exception
                val errorType = DisconnectionError()
                when (type) {
                    TYPE_GET_PHARMACY_ADDRESSES -> pharmacyAddressesViewModel.setResult(
                        result = ErrorResult(
                            exception = currentException
                        ), errorType = errorType
                    )
                }
                getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) =
        with(binding.layoutPendingResultPharmacyAddresses) {
            when (flag) {
                FLAG_PENDING_RESULT -> {
                    root.visibility = View.VISIBLE
                    bTryAgain.visibility = View.INVISIBLE
                    tvErrorMessage.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                }

                FLAG_SUCCESS_RESULT -> {
                    root.visibility = View.GONE
                    bTryAgain.visibility = View.INVISIBLE
                    tvErrorMessage.visibility = View.INVISIBLE
                    progressBar.visibility = View.INVISIBLE
                }

                FLAG_ERROR_RESULT -> {
                    root.visibility = View.VISIBLE
                    bTryAgain.visibility = View.VISIBLE
                    tvErrorMessage.visibility = View.VISIBLE
                    tvErrorMessage.text = messageError
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }

    private fun updateCounter() {
        var counter = 0
        pharmacyAddressesViewModel.listSelectedPharmacyAddresses.value?.forEach {
            if (it.isSelected) counter++
        }
        pharmacyAddressesViewModel.setCounterSelectedItems(counter = counter)
    }

    private fun onClickMenuItem() {
        if (pharmacyAddressesViewModel.result.value is ErrorResult ||
            pharmacyAddressesViewModel.result.value is PendingResult
        ) {
            val errorType = pharmacyAddressesViewModel.errorType.value
            val message = getString(getMessageByErrorType(errorType = errorType))
            getSupportActivity().showToast(message = message)
            return
        }
        val mutableListSelectedPharmacyAddresses =
            pharmacyAddressesViewModel.listSelectedPharmacyAddresses.value
                ?: throw NullPointerException("PharmacyAddressesFragment mutableListSelectedPharmacyAddresses = null")

        val arrayListPharmacyAddressesId = arrayListOf<Int>()

        val mutableListOnlySelectedPharmacyAddresses = mutableListOf<PharmacyAddressesModel>()
        mutableListSelectedPharmacyAddresses.forEach { selectedPharmacyAddressesModel ->
            if (selectedPharmacyAddressesModel.isSelected) mutableListOnlySelectedPharmacyAddresses.add(
                selectedPharmacyAddressesModel.pharmacyAddressesModel
            )
        }
        Log.i("TAG","mutableListOnlySelectedPharmacyAddresses = $mutableListOnlySelectedPharmacyAddresses")

        mutableListOnlySelectedPharmacyAddresses.forEach { pharmacyAddressesModel ->
            val addressId = pharmacyAddressesModel.addressId
            arrayListPharmacyAddressesId.add(addressId)
        }
        Log.i("TAG","arrayListPharmacyAddressesId = $arrayListPharmacyAddressesId")

        val bundle = Bundle()
        bundle.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListPharmacyAddressesId)
        navControllerCatalog.navigate(R.id.action_pharmacyAddressesFragment_to_filterFragment, bundle, navOptions {
            popUpTo(R.id.filterFragment) {
                inclusive = true
            }
        })
    }

}