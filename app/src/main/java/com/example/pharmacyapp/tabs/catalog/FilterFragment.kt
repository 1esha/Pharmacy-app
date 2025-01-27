package com.example.pharmacyapp.tabs.catalog


import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
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
import com.example.domain.catalog.CatalogResult
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_FILTERED
import com.example.pharmacyapp.KEY_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.KEY_DEFAULT_PRICE_FROM
import com.example.pharmacyapp.KEY_DEFAULT_PRICE_UP_TO
import com.example.pharmacyapp.KEY_IS_CHECKED_DISCOUNT
import com.example.pharmacyapp.KEY_PATH
import com.example.pharmacyapp.KEY_PRICE_FROM
import com.example.pharmacyapp.KEY_PRICE_UP_TO
import com.example.pharmacyapp.KEY_RESULT_ARRAY_LIST_IDS_FILTERED
import com.example.pharmacyapp.KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.MenuSettingsModel
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentFilterBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getPrice
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.FilterViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.FilterViewModelFactory
import com.example.pharmacyapp.toArrayListInt
import java.lang.Exception
import kotlin.math.roundToInt

class FilterFragment : Fragment(), CatalogResult, View.OnKeyListener {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val filterViewModel: FilterViewModel by viewModels(
        factoryProducer = { FilterViewModelFactory()}
    )

    private lateinit var navControllerCatalog: NavController

    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        path = arguments?.getString(KEY_PATH) ?:
        throw NullPointerException("FilterFragment path = null")

        val isShownGetProductsByPath = filterViewModel.isShownGetProductsByPath.value?:
        throw NullPointerException("FilterFragment isShownGetProductsByPath = null")

        val isShownGetProductAvailabilityByPath = filterViewModel.isShownGetProductAvailabilityByPath.value ?:
        throw NullPointerException("FilterFragment isShownGetProductAvailabilityByPath = null")

        if(!isShownGetProductsByPath){
            onSuccessfulEvent(type = TYPE_GET_PRODUCTS_BY_PATH) {

                filterViewModel.getProductsByPath(path = path)

                if (!isShownGetProductAvailabilityByPath) {
                    onSuccessfulEvent(type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH) {
                        filterViewModel.getProductAvailabilityByPath(path = path)
                    }
                }
            }
        }

        setFragmentResultListener(KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES) { requestKey, bundle ->

            val arrayListCurrentIdsSelectedAddresses = bundle.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()

            arguments?.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListCurrentIdsSelectedAddresses)

            Log.i("TAG","FilterFragment onCreate arrayListCurrentIdsSelectedAddresses = $arrayListCurrentIdsSelectedAddresses")

            onSuccessfulEvent(type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH) {
                filterViewModel.getProductAvailabilityByPath(path = path)
            }

            installInitialUI()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        installInitialUI()

        navControllerCatalog = findNavController()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.filters),
            icon = R.drawable.ic_back
        ) {
            navControllerCatalog.navigateUp()
        })
        toolbarViewModel.clearMenu()
        toolbarViewModel.installMenu(menuSettingsModel = MenuSettingsModel(
            menu = R.menu.menu_delete_filters
        ) { menuItemId ->
            if (menuItemId == R.id.deleteFilters) {
                clearFilters()
            }
        })

        layoutAvailabilityInPharmacies.setOnClickListener {

            filterViewModel.setIsShownGetProductAvailabilityByPath(isShown = false)

            val arrayListIdsSelectedAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES)

            val bundle = Bundle()

            bundle.putString(KEY_PATH, path)
            bundle.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES, arrayListIdsSelectedAddresses)

            navControllerCatalog.navigate(R.id.action_filterFragment_to_pharmacyAddressesFragment, bundle)
        }

        bShowFilteredProducts.setOnClickListener {

            onEnterPrice()

            val arrayListIdsSelectedAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES)
            val from = etFrom.text.toString().toInt()
            val upTo = etUpTo.text.toString().toInt()
            val isChecked = checkBoxDiscountedProduct.isChecked

            val arrayListIdsFilteredProducts = getFilteredArrayList(
                isChecked = isChecked,
                priceFrom = from,
                priceUpTo = upTo,
                arrayListSelectedIdsAddresses = arrayListIdsSelectedAddresses ?: arrayListOf<Int>()
            )

            Log.i("TAG","from = $from\nup to = $upTo")
            Log.i("TAG","finish = $arrayListIdsFilteredProducts")

            val bundle = Bundle()
            bundle.putIntegerArrayList(KEY_ARRAY_LIST_IDS_FILTERED,arrayListIdsFilteredProducts)

            bundle.putBoolean(KEY_IS_CHECKED_DISCOUNT,isChecked)
            bundle.putInt(KEY_PRICE_FROM,from)
            bundle.putInt(KEY_PRICE_UP_TO,upTo)
            bundle.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListIdsSelectedAddresses)

            setFragmentResult(KEY_RESULT_ARRAY_LIST_IDS_FILTERED,bundle)

            navControllerCatalog.popBackStack()

        }

        layoutPendingResultFilter.bTryAgain.setOnClickListener {

            with(filterViewModel) {
                setIsShownGetProductAvailabilityByPath(isShown = false)
                setIsShownGetProductsByPath(isShown = false)
            }

            onSuccessfulEvent(type = TYPE_GET_PRODUCTS_BY_PATH) {
                filterViewModel.getProductsByPath(path = path)
                onSuccessfulEvent(type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH) {
                    filterViewModel.getProductAvailabilityByPath(path = path)
                }
            }

        }

        etUpTo.setOnKeyListener(this@FilterFragment)

        etFrom.setOnKeyListener(this@FilterFragment)

        filterViewModel.mediatorFilter.observe(viewLifecycleOwner) { mediatorResult ->
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
                    val errorType = filterViewModel.errorType.value
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

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyEvent?.action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                onEnterPrice()
            }
        }
        return false
    }

    override fun onPendingResultListener() {
        Log.i("TAG","FilterFragment onPendingResultListener")
        filterViewModel.clearErrorType()
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(value: T, type: String?) {

        when(type) {
            TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH -> {
                val isShownGetProductAvailabilityByPath = filterViewModel.isShownGetProductAvailabilityByPath.value
                    ?: throw NullPointerException("FilterFragment isShownGetProductAvailabilityByPath = null")

                if (!isShownGetProductAvailabilityByPath) {
                    Log.i("TAG","FilterFragment onSuccessResultListener TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH")
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {

                        val listAllProductAvailabilityModel = responseValueModel.value as MutableList<*>
                        Log.i("TAG","FilterFragment listAllProductAvailabilityModel = $listAllProductAvailabilityModel")

                        filterViewModel.setListAllIdsProductsAvailability(list = listAllProductAvailabilityModel)

                        updateUI(flag = FLAG_SUCCESS_RESULT)
                    }
                    else {
                        filterViewModel.setResultGetProductAvailabilityByPath(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                filterViewModel.setIsShownGetProductAvailabilityByPath(isShown = true)

            }
            TYPE_GET_PRODUCTS_BY_PATH -> {
                val isShownGetProductsByPath = filterViewModel.isShownGetProductsByPath.value?: throw NullPointerException("FilterFragment isShownGetProductsByPath = null")
                if (!isShownGetProductsByPath) {
                    Log.i("TAG","FilterFragment onSuccessResultListener TYPE_GET_PRODUCTS_BY_PATH")
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {

                        val listProducts = responseValueModel.value as List<*>

                        filterViewModel.setListAllProducts(listProducts = listProducts)

                    }
                    else {
                        filterViewModel.setResultGetProductsByPath(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }
                filterViewModel.setIsShownGetProductsByPath(isShown = true)
            }
        }

    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG","FilterFragment onErrorResultListener")
        filterViewModel.setIsShownGetProductsByPath(isShown = true)
        filterViewModel.setIsShownGetProductAvailabilityByPath(isShown = true)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = message)
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
                    TYPE_GET_PRODUCTS_BY_PATH -> filterViewModel.setResultGetProductsByPath(result = PendingResult())
                    TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH ->filterViewModel.setResultGetProductAvailabilityByPath(result = PendingResult())
                }

                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()

                when(type) {
                    TYPE_GET_PRODUCTS_BY_PATH -> filterViewModel.setResultGetProductsByPath(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH -> filterViewModel.setResultGetProductAvailabilityByPath(result = ErrorResult(exception = currentException), errorType = errorType)
                }

                getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultFilter) {
        when(flag) {
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

    private fun installInitialUI() = with(binding) {
        val isChecked = arguments?.getBoolean(KEY_IS_CHECKED_DISCOUNT) ?: false
        val arrayListIdsSelectedAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()
        val priceFrom = arguments?.getInt(KEY_PRICE_FROM) ?: -1
        val priceUpTo = arguments?.getInt(KEY_PRICE_UP_TO) ?: -1
        val defaultPriceUpTo = arguments?.getInt(KEY_DEFAULT_PRICE_UP_TO)
        val defaultPriceFrom = arguments?.getInt(KEY_DEFAULT_PRICE_FROM)

        checkBoxDiscountedProduct.isChecked = isChecked

        val numberSelectedAddresses = arrayListIdsSelectedAddresses.size
        if (numberSelectedAddresses > 0) {
            layoutSelectedAddresses.visibility = View.VISIBLE
            val textNumberSelectedAddresses = numberSelectedAddresses.toString()
            tvNumberSelectedAddresses.text = textNumberSelectedAddresses
        }
        else {
            layoutSelectedAddresses.visibility = View.GONE
        }

        val textDefaultPriceUpTo = defaultPriceUpTo.toString()
        val textDefaultPriceFrom = defaultPriceFrom.toString()

        val textPriceUpTo = priceUpTo.toString()
        val textPriceFrom = priceFrom.toString()
        if (priceFrom <= 0) {
            etFrom.setText(textDefaultPriceFrom)
        } else{
            etFrom.setText(textPriceFrom)
        }

        if (priceUpTo <= 0) {
            etUpTo.setText(textDefaultPriceUpTo)
        } else{
            etUpTo.setText(textPriceUpTo)
        }

    }

    private fun clearFilters() {

        val resultGetProductsByPath = filterViewModel.resultGetProductsByPath.value ?:
        throw NullPointerException("FilterFragment resultGetProductsByPath = null")

        val resultGetProductAvailabilityByPath = filterViewModel.resultGetProductAvailabilityByPath.value ?:
        throw NullPointerException("FilterFragment resultGetProductAvailabilityByPath = null")

        if (resultGetProductsByPath.result is ErrorResult ||
            resultGetProductsByPath.result is PendingResult ||
            resultGetProductAvailabilityByPath.result is ErrorResult ||
            resultGetProductAvailabilityByPath.result is PendingResult) {
            val errorType = filterViewModel.errorType.value
            val message = getString(getMessageByErrorType(errorType = errorType))
            getSupportActivity().showToast(message = message)
        }
        else {
            arguments?.putBoolean(KEY_IS_CHECKED_DISCOUNT, false)
            arguments?.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListOf<Int>())
            arguments?.putInt(KEY_PRICE_FROM, -1)
            arguments?.putInt(KEY_PRICE_UP_TO, -1)

            installInitialUI()
        }

    }

    private fun onEnterPrice() =
        with(binding) {

            val defaultPriceUpTo = arguments?.getInt(KEY_DEFAULT_PRICE_UP_TO)
            val defaultPriceFrom = arguments?.getInt(KEY_DEFAULT_PRICE_FROM)

            val textDefaultPriceUpTo = defaultPriceUpTo.toString()
            val textDefaultPriceFrom = defaultPriceFrom.toString()

            val currentPriceFrom  = if (etFrom.text.toString().isEmpty() || etFrom.text.toString().isBlank()) {
                etFrom.setText(textDefaultPriceFrom)
                defaultPriceFrom
            }
            else {
                etFrom.text.toString().toDouble().roundToInt()
            }

            val currentPriceUpTo = if (etUpTo.text.toString().isEmpty() || etUpTo.text.toString().isBlank()) {
                etUpTo.setText(textDefaultPriceUpTo)
                defaultPriceUpTo
            }
            else {
                etUpTo.text.toString().toDouble().roundToInt()
            }

            if (currentPriceFrom!! < defaultPriceFrom!!) {
                etFrom.setText(textDefaultPriceFrom)
            }
            if (currentPriceFrom > defaultPriceUpTo!!) {
                etFrom.setText(textDefaultPriceFrom)
            }
            if (currentPriceUpTo!! > defaultPriceUpTo) {
                etUpTo.setText(textDefaultPriceUpTo)
            }
            if (currentPriceUpTo < defaultPriceFrom) {
                etUpTo.setText(textDefaultPriceUpTo)
            }
            if (currentPriceFrom > currentPriceUpTo) {
                etUpTo.setText(textDefaultPriceUpTo)
            }
        }

    private fun getFilteredArrayList(
        isChecked: Boolean,
        priceFrom: Int,
        priceUpTo: Int,
        arrayListSelectedIdsAddresses: ArrayList<Int>
    ): ArrayList<Int> {

        val listAllProducts = filterViewModel.listAllProducts.value
            ?: throw NullPointerException("FilterFragment listAllProducts = null")

        val listAllIdsProductsAvailability = filterViewModel.listAllIdsProductsAvailability.value
            ?: throw NullPointerException("FilterFragment listAllIdsProductsAvailability = null")

        val mutableListIdsProductsFiltered = mutableListOf<Int>()

        val mutableListOnlyIdsProductsAvailability = mutableListOf<Int>()

        Log.i("TAG","FilterFragment getFilteredArrayList isChecked = $isChecked")
        Log.i("TAG","FilterFragment getFilteredArrayList priceFrom = $priceFrom")
        Log.i("TAG","FilterFragment getFilteredArrayList priceUpTo = $priceUpTo")
        Log.i("TAG","FilterFragment getFilteredArrayList arrayListSelectedIdsAddresses = $arrayListSelectedIdsAddresses")
        Log.i("TAG","FilterFragment getFilteredArrayList listAllIdsProductsAvailability = $listAllIdsProductsAvailability")

        // получения списка id товаров которые в наличии в аптеках
        if (arrayListSelectedIdsAddresses.size == 0) {
            listAllIdsProductsAvailability.forEach {
                val productsAvailabilityModel = it as ProductAvailabilityModel
                if (productsAvailabilityModel.numberProducts > 0) {
                    mutableListOnlyIdsProductsAvailability.add(productsAvailabilityModel.productId)
                }
            }
        }
        else {
            listAllIdsProductsAvailability.forEach {
                val productsAvailabilityModel = it as ProductAvailabilityModel
                if (arrayListSelectedIdsAddresses.contains(productsAvailabilityModel.addressId)) {
                    if (productsAvailabilityModel.numberProducts > 0) {
                        mutableListOnlyIdsProductsAvailability.add(productsAvailabilityModel.productId)
                        mutableListOnlyIdsProductsAvailability.distinct()
                    }
                }

            }
        }

        Log.i("TAG","FilterFragment getFilteredArrayList mutableListOnlyIdsProductsAvailability = ${mutableListOnlyIdsProductsAvailability.distinct()}")

        if (isChecked) {
            // получение списка id товаров со скидкой и в наличии в выбранных аптеках
            listAllProducts.forEach {
                val productModel = it as ProductModel
                val price = getPrice(
                    discount = productModel.discount,
                    price = productModel.price
                )
                Log.i("TAG","getFilteredArrayList price = $price\npriceFrom = $priceFrom\npriceUpTo = $priceUpTo")
                if (productModel.discount > 0) {
                    if (price in priceFrom..priceUpTo) {
                        if (mutableListOnlyIdsProductsAvailability.distinct().contains(productModel.product_id)) {
                            mutableListIdsProductsFiltered.add(productModel.product_id)
                            // mutableListIdsProductsFiltered содержит только id товаров со скидкой и в наличии в выбранных аптеках
                        }

                    }
                }

            }

        }
        else {
            // получение списка id товаров без скидки, но в наличии в выбранных аптеках
            listAllProducts.forEach {
                val productModel = it as ProductModel

                val price = getPrice(
                    discount = productModel.discount,
                    price = productModel.price
                )
                Log.i("TAG","getFilteredArrayList price = $price\npriceFrom = $priceFrom\npriceUpTo = $priceUpTo")
                if (price in priceFrom..priceUpTo) {

                    if (mutableListOnlyIdsProductsAvailability.contains(productModel.product_id)) {
                        mutableListIdsProductsFiltered.add(productModel.product_id)
                    }
                }
            }

        }
        Log.i("TAG","FilterFragment getFilteredArrayList isChecked = $isChecked mutableListIdsProductsFiltered = ${mutableListIdsProductsFiltered}")

        return mutableListIdsProductsFiltered.toArrayListInt()
    }

}