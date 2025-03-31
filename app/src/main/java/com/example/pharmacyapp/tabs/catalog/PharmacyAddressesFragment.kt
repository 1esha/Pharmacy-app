package com.example.pharmacyapp.tabs.catalog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.models.RequestModel
import com.example.domain.models.SelectedPharmacyAddressesModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.KEY_PATH
import com.example.pharmacyapp.KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentPharmacyAddressesBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.PharmacyAddressesAdapter
import com.example.pharmacyapp.tabs.catalog.viewmodels.PharmacyAddressesViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.PharmacyAddressesViewModelFactory
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Класс [PharmacyAddressesFragment] - является фрагментом экрана для выбора адресов аптек
 */
class PharmacyAddressesFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentPharmacyAddressesBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val pharmacyAddressesViewModel: PharmacyAddressesViewModel by viewModels(
        factoryProducer = { PharmacyAddressesViewModelFactory() }
    )

    private lateinit var pharmacyAddressesAdapter: PharmacyAddressesAdapter

    private lateinit var navControllerCatalog: NavController

    val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(pharmacyAddressesViewModel) {

            navControllerCatalog = findNavController()

            initValues(
                path = arguments?.getString(KEY_PATH),
                arrayListSelectedIdsAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()
            )

            sendingRequests(isNetworkStatus = isNetworkStatus)

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    stateScreen.collect { result ->
                        when (result) {
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
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    counter.collect { counter ->
                        installToolbar(counter = counter)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    listSelectedPharmacyAddresses.collect { listSelectedPharmacyAddresses ->
                        installAdapter(listSelectedPharmacyAddressesModel = listSelectedPharmacyAddresses)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPharmacyAddressesBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        fabSelectAddresses.setOnClickListener {
            pharmacyAddressesViewModel.transmittingArrayListSelectedIds{ arrayListPharmacyAddressesId ->

                val bundle = Bundle().apply {
                    putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES, arrayListPharmacyAddressesId)
                }

                setFragmentResult(KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES, bundle)

                navControllerCatalog.popBackStack()
            }
        }

        layoutPendingResultPharmacyAddresses.bTryAgain.setOnClickListener {
            pharmacyAddressesViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        pharmacyAddressesViewModel.setIsInstallAdapter(isInstallAdapter = true)
    }

    override fun onErrorResultListener(exception: Exception) {
        Log.i("TAG", "PharmacyAddressesFragment onErrorResultListener")
        toolbarViewModel.clearMenu()
        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        Log.i("TAG", "PharmacyAddressesFragment onPendingResultListener")
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(data: T): Unit = with(pharmacyAddressesViewModel) {
        Log.i("TAG", "PharmacyAddressesFragment onSuccessResultListener")
        try {
            val _listRequests = data as List<*>
            val listRequests = _listRequests.map { request ->
                return@map request as RequestModel
            }
            Log.i("TAG", "listRequests = $listRequests")

            var fullType = ""
            listRequests.forEach { request ->
                fullType += request.type
            }

            when (fullType) {
                TYPE_GET_PHARMACY_ADDRESSES + TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH -> {
                    Log.i("TAG", "fullType =   TYPE_GET_PHARMACY_ADDRESSES + TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH")
                    val resultGetPharmacyAddresses =
                        listRequests.find { it.type == TYPE_GET_PHARMACY_ADDRESSES }?.result!!.asSuccess()!!

                    val resultGetProductAvailabilityByPath =
                        listRequests.find { it.type == TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH }?.result!!.asSuccess()!!


                    val responseGetPharmacyAddresses =
                        resultGetPharmacyAddresses.data as ResponseValueModel<*>

                    val responseGetProductAvailabilityByPath =
                        resultGetProductAvailabilityByPath.data as ResponseValueModel<*>


                    val _listPharmacyAddresses = responseGetPharmacyAddresses.value as List<*>
                    val listPharmacyAddresses =
                        _listPharmacyAddresses.map { it as PharmacyAddressesModel }

                    val _listProductAvailability =
                        responseGetProductAvailabilityByPath.value as List<*>
                    val listProductAvailability =
                        _listProductAvailability.map { it as ProductAvailabilityModel }

                    fillingList(
                        listPharmacyAddresses = listPharmacyAddresses,
                        listProductAvailability = listProductAvailability
                    )

                    updateCounter()
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        } catch (e: Exception) {
            Log.e("TAG", e.stackTraceToString())
        }
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

    private fun installToolbar(counter: Int) = with(toolbarViewModel){
        installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.selected, counter.toString()),
            icon = R.drawable.ic_back
        ) {
            navControllerCatalog.navigateUp()
        })

        clearMenu()
        if (counter > 0) {
            inflateMenu(menu = R.menu.menu_select_pharmacy_addresses)
            setMenuClickListener {
                clearAddressesSelected()
            }
        }
    }

    private fun installAdapter(listSelectedPharmacyAddressesModel: List<SelectedPharmacyAddressesModel>) = with(binding) {
        try {
            pharmacyAddressesViewModel.installAdapter {
                pharmacyAddressesAdapter = PharmacyAddressesAdapter(
                    mutableListSelectedPharmacyAddressesModel = listSelectedPharmacyAddressesModel.toMutableList(),
                    onClick = ::onSelectAddress
                )

                rvPharmacyAddresses.adapter = pharmacyAddressesAdapter
                rvPharmacyAddresses.layoutManager = LinearLayoutManager(requireContext())
            }

        } catch (e: Exception) {
            Log.e("TAG", e.stackTraceToString())
        }
    }

    private fun onSelectAddress(addressId: Int, isSelect: Boolean) = with(pharmacyAddressesViewModel) {
        onSelectAddress(
            addressId = addressId,
            isSelect = isSelect
        )

        updateCounter()
    }

    private fun clearAddressesSelected() = with(pharmacyAddressesViewModel) {
        clearAddressesSelected()

        updateCounter()
    }

}