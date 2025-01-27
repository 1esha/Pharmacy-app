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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.catalog.CatalogResult
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.models.PharmacyAddressesModel
import com.example.domain.models.SelectedPharmacyAddressesModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.KEY_PATH
import com.example.pharmacyapp.KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.MenuSettingsModel
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentPharmacyAddressesBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.PharmacyAddressesAdapter
import com.example.pharmacyapp.tabs.catalog.viewmodels.PharmacyAddressesViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.PharmacyAddressesViewModelFactory
import java.lang.Exception
import kotlin.properties.Delegates

class PharmacyAddressesFragment : Fragment(), CatalogResult {

    private var _binding: FragmentPharmacyAddressesBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val pharmacyAddressesViewModel: PharmacyAddressesViewModel by viewModels(
        factoryProducer = { PharmacyAddressesViewModelFactory()}
    )

    private lateinit var arrayListIdsAddresses: ArrayList<Int>

    private var path: String by Delegates.notNull()

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

        path = arguments?.getString(KEY_PATH) ?: throw NullPointerException("PharmacyAddressesFragment path = null")

        arrayListIdsAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()

        navControllerCatalog = findNavController()

        pharmacyAddressesViewModel.counterSelectedItems.observe(viewLifecycleOwner) { counter ->
            toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
                title = getString(R.string.selected, counter.toString()),
                icon = R.drawable.ic_back
            ) {
                navControllerCatalog.navigateUp()
            })

            if (counter > 0) {
                toolbarViewModel.clearMenu()
                toolbarViewModel.installMenu(menuSettingsModel = MenuSettingsModel(
                    menu = R.menu.menu_select_pharmacy_addresses
                ) {
                    clearAddressesSelected()
                })
            }
            else {
                toolbarViewModel.clearMenu()
            }
        }

        val isShownGetPharmacyAddresses = pharmacyAddressesViewModel.isShownGetPharmacyAddresses.value
            ?: throw NullPointerException("PharmacyAddressesFragment isShownGetPharmacyAddresses = null")

        if (!isShownGetPharmacyAddresses) {

            onSuccessfulEvent(type = TYPE_GET_PHARMACY_ADDRESSES) {
                pharmacyAddressesViewModel.getPharmacyAddresses()
            }

        }

        fabSelectAddresses.setOnClickListener {
            onClickSelectAddresses()
        }

        layoutPendingResultPharmacyAddresses.bTryAgain.setOnClickListener {
            onSuccessfulEvent(type = TYPE_GET_PHARMACY_ADDRESSES) {
                with(pharmacyAddressesViewModel) {
                    getPharmacyAddresses()
                    setIsShownGetPharmacyAddresses(isShown = false)
                    setIsShownGetProductAvailabilityByPath(isShown = false)
                }
            }
        }

        pharmacyAddressesViewModel.mediatorPharmacyAddresses.observe(viewLifecycleOwner) { mediatorResult ->

            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResultListener()}
                is SuccessResult -> {
                    onSuccessResultListener(
                        value = result.value,
                        type = type
                    )
                }
                is ErrorResult -> {
                    val errorType = pharmacyAddressesViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }

        pharmacyAddressesViewModel.mutableListSelectedPharmacyAddresses.observe(viewLifecycleOwner) { mutableList ->

            pharmacyAddressesViewModel.setInitPharmacyAddresses(list = mutableList, counter = arrayListIdsAddresses.size)

            val pharmacyAddressesAdapter = getSetupAdapter(mutableList = mutableList)

            with(binding) {
                rvPharmacyAddresses.adapter = pharmacyAddressesAdapter
                rvPharmacyAddresses.layoutManager = LinearLayoutManager(requireContext())
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(value: T, type: String?) {
        Log.i("TAG", "PharmacyAddressesFragment onSuccessResultListener")

        when(type) {
            TYPE_GET_PHARMACY_ADDRESSES -> {
                val isShown = pharmacyAddressesViewModel.isShownGetPharmacyAddresses.value
                    ?: throw NullPointerException("PharmacyAddressesFragment isShownGetPharmacyAddresses = null")
                if (!isShown) {
                    Log.i("TAG", "PharmacyAddressesFragment onSuccessResultListener TYPE_GET_PHARMACY_ADDRESSES")
                    val responseValueModel = value as ResponseValueModel<*>
                    val responseModel = responseValueModel.responseModel
                    val status = responseModel.status
                    val message = responseModel.message

                    if (status in 200..299) {

                        val listAllPharmacyAddresses = responseValueModel.value as List<*> // получения списка всех адресов аптек

                        pharmacyAddressesViewModel.setListPharmacyAddresses(list = listAllPharmacyAddresses)

                        onSuccessfulEvent(type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH) {
                            pharmacyAddressesViewModel.getProductAvailabilityByPath(path = path)
                        }

                    } else {
                        pharmacyAddressesViewModel.setResultGetPharmacyAddresses(
                            result = ErrorResult(exception = Exception()),
                            errorType = OtherError()
                        )
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                pharmacyAddressesViewModel.setIsShownGetPharmacyAddresses(isShown = true)
            }
            TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH -> {
                val isShown = pharmacyAddressesViewModel.isShownGetProductAvailabilityByPath.value
                    ?: throw NullPointerException("PharmacyAddressesFragment isShownGetProductAvailabilityByPath = null")
                if (!isShown) {
                    Log.i("TAG", "PharmacyAddressesFragment onSuccessResultListener TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH")
                    val responseValueModel = value as ResponseValueModel<*>
                    val responseModel = responseValueModel.responseModel
                    val status = responseModel.status
                    val message = responseModel.message

                    if (status in 200..299) {

                        val listProductAvailabilityModel = responseValueModel.value as List<*> // получения списка с данными о наличии товаров в аптеках

                        installSelectedProductAvailability(listProductAvailabilityModel = listProductAvailabilityModel)

                        updateUI(flag = FLAG_SUCCESS_RESULT)
                    }
                    else {
                        pharmacyAddressesViewModel.setResultGetProductAvailabilityByPath(
                            result = ErrorResult(exception = Exception()),
                            errorType = OtherError()
                        )
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }
                pharmacyAddressesViewModel.setIsShownGetProductAvailabilityByPath(isShown = true)

                }

            }

        }


    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG", "PharmacyAddressesFragment onErrorResultListener")
        updateUI(FLAG_ERROR_RESULT, messageError = message)

        pharmacyAddressesViewModel.setIsShownGetProductAvailabilityByPath(isShown = true)
        pharmacyAddressesViewModel.setIsShownGetPharmacyAddresses(isShown = true)

    }

    override fun onPendingResultListener() {
        Log.i("TAG", "PharmacyAddressesFragment onPendingResultListener")
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
                    TYPE_GET_PHARMACY_ADDRESSES -> pharmacyAddressesViewModel.setResultGetPharmacyAddresses(result = PendingResult())
                    TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH -> pharmacyAddressesViewModel.setResultGetProductAvailabilityByPath(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else exception
                val errorType = DisconnectionError()
                when (type) {
                    TYPE_GET_PHARMACY_ADDRESSES -> pharmacyAddressesViewModel.setResultGetPharmacyAddresses(
                        result = ErrorResult(
                            exception = currentException
                        ), errorType = errorType
                    )
                    TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH -> pharmacyAddressesViewModel.setResultGetProductAvailabilityByPath(
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

    private fun getSetupAdapter(mutableList: MutableList<SelectedPharmacyAddressesModel>): PharmacyAddressesAdapter {
        return PharmacyAddressesAdapter(listItems = mutableList) { position, isSelect ->

            pharmacyAddressesViewModel.setPharmacyAddresses(
                position = position,
                isSelect = isSelect
            )

            updateCounter()

            Log.i("TAG","PharmacyAddressesFragment mutableList = $mutableList")

        }
    }

    private fun updateCounter() {
        var counter = 0
        pharmacyAddressesViewModel.mutableListSelectedPharmacyAddresses.value?.forEach {
            if (it.isSelected) counter++
        }
        pharmacyAddressesViewModel.setCounterSelectedItems(counter = counter)
    }

    // установка списка с наличием товаров в аптеках
    private fun installSelectedProductAvailability(listProductAvailabilityModel: List<*>) {

        val listAllPharmacyAddressesModel = pharmacyAddressesViewModel.listPharmacyAddresses.value ?: listOf<Any>()

        val mutableListSelectedPharmacyAddressesModel = mutableListOf<SelectedPharmacyAddressesModel>()

        listAllPharmacyAddressesModel.forEach { _pharmacyAddressesModel ->

            val pharmacyAddressesModel = _pharmacyAddressesModel as PharmacyAddressesModel

            listProductAvailabilityModel.forEach { _productAvailabilityModel ->

                val productAvailabilityModel = _productAvailabilityModel as ProductAvailabilityModel

                val isSelected = if (arrayListIdsAddresses.contains(pharmacyAddressesModel.addressId)) true else false

                if (mutableListSelectedPharmacyAddressesModel.none { it.pharmacyAddressesModel.addressId == pharmacyAddressesModel.addressId }) {

                    if (pharmacyAddressesModel.addressId == productAvailabilityModel.addressId) {
                        if (productAvailabilityModel.numberProducts > 0) {
                            mutableListSelectedPharmacyAddressesModel.add(
                                SelectedPharmacyAddressesModel(
                                    isSelected = isSelected,
                                    pharmacyAddressesModel = pharmacyAddressesModel,
                                    productAvailabilityModel = productAvailabilityModel
                                )
                            )
                        }
                    }
                }

            }
        }

        Log.i("TAG","PharmacyAddressesFragment installSelectedProductAvailability mutableListSelectedPharmacyAddressesModel = $mutableListSelectedPharmacyAddressesModel")
        pharmacyAddressesViewModel.setMutableListSelectedPharmacyAddresses(mutableList = mutableListSelectedPharmacyAddressesModel)
    }

    private fun clearAddressesSelected() {
        val mutableListCurrentSelectedAddresses = pharmacyAddressesViewModel.mutableListSelectedPharmacyAddresses.value ?:
        throw NullPointerException("PharmacyAddressesFragment mutableListCurrentSelectedAddresses = null")

        val mutableListClearedAddresses = mutableListOf<SelectedPharmacyAddressesModel>()
        mutableListCurrentSelectedAddresses.forEach { selectedPharmacyAddressesModel ->
            mutableListClearedAddresses.add(SelectedPharmacyAddressesModel(
                isSelected = false,
                pharmacyAddressesModel = selectedPharmacyAddressesModel.pharmacyAddressesModel,
                productAvailabilityModel = selectedPharmacyAddressesModel.productAvailabilityModel
            ))
        }
        pharmacyAddressesViewModel.setCounterSelectedItems(counter = 0)
        pharmacyAddressesViewModel.setMutableListSelectedPharmacyAddresses(mutableList = mutableListClearedAddresses)
    }

    private fun onClickSelectAddresses() {
        if (pharmacyAddressesViewModel.resultGetPharmacyAddresses.value?.result is ErrorResult ||
            pharmacyAddressesViewModel.resultGetPharmacyAddresses.value?.result is PendingResult ||
            pharmacyAddressesViewModel.resultGetProductAvailabilityByPath.value?.result is ErrorResult ||
            pharmacyAddressesViewModel.resultGetProductAvailabilityByPath.value?.result is PendingResult
        ) {
            val errorType = pharmacyAddressesViewModel.errorType.value
            val message = getString(getMessageByErrorType(errorType = errorType))
            getSupportActivity().showToast(message = message)
            return
        }
        val mutableListSelectedPharmacyAddresses = pharmacyAddressesViewModel.mutableListSelectedPharmacyAddresses.value
                ?: throw NullPointerException("PharmacyAddressesFragment mutableListSelectedPharmacyAddresses = null")

        val arrayListPharmacyAddressesId = arrayListOf<Int>()

        val mutableListOnlySelectedPharmacyAddresses = mutableListOf<PharmacyAddressesModel>()

        // добваление в mutableListOnlySelectedPharmacyAddresses только только выбранные модели адресов
        mutableListSelectedPharmacyAddresses.forEach { selectedPharmacyAddressesModel ->
            if (selectedPharmacyAddressesModel.isSelected) mutableListOnlySelectedPharmacyAddresses.add(selectedPharmacyAddressesModel.pharmacyAddressesModel)
        }
        Log.i("TAG","PharmacyAddressesFragment mutableListOnlySelectedPharmacyAddresses = $mutableListOnlySelectedPharmacyAddresses")

        // добавление в arrayListPharmacyAddressesId только id выбранных моделей адресов
        mutableListOnlySelectedPharmacyAddresses.forEach { pharmacyAddressesModel ->
            val addressId = pharmacyAddressesModel.addressId
            arrayListPharmacyAddressesId.add(addressId)
        }
        Log.i("TAG","PharmacyAddressesFragment arrayListPharmacyAddressesId = $arrayListPharmacyAddressesId")

        val bundle = Bundle()

        bundle.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListPharmacyAddressesId)
        setFragmentResult(KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES, bundle)

        navControllerCatalog.popBackStack()

    }

}