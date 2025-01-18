package com.example.pharmacyapp.tabs.catalog

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.catalog.CatalogResult
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
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentProductsBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getPrice
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.ProductsAdapter
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductsViewModel
import java.lang.Exception


class ProductsFragment : Fragment(), CatalogResult {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val productsViewModel: ProductsViewModel by viewModels()

    private lateinit var navControllerCatalog: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(KEY_RESULT_ARRAY_LIST_IDS_FILTERED) { requestKey, bundle ->
            val arrayListIdsFiltered = bundle.getIntegerArrayList(KEY_ARRAY_LIST_IDS_FILTERED) ?: arrayListOf<Int>()

            val isChecked = bundle.getBoolean(KEY_IS_CHECKED_DISCOUNT)
            val priceFrom = bundle.getInt(KEY_PRICE_FROM)
            val priceUpTo = bundle.getInt(KEY_PRICE_UP_TO)
            val arrayListIdsSelectedAddresses = bundle.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()

            arguments?.putBoolean(KEY_IS_CHECKED_DISCOUNT,isChecked)
            arguments?.putInt(KEY_PRICE_FROM,priceFrom)
            arguments?.putInt(KEY_PRICE_UP_TO,priceUpTo)
            arguments?.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListIdsSelectedAddresses)

            Log.i("TAG","ProductsFragment onCreate arrayListIdsFiltered = $arrayListIdsFiltered")
            Log.i("TAG","ProductsFragment onCreate isChecked = $isChecked")
            Log.i("TAG","ProductsFragment onCreate priceFrom = $priceFrom")
            Log.i("TAG","ProductsFragment onCreate priceUpTo = $priceUpTo")
            Log.i("TAG","ProductsFragment onCreate arrayListIdsSelectedAddresses = $arrayListIdsSelectedAddresses")

            val listProduct = productsViewModel.listAllProducts.value ?:
            throw NullPointerException("ProductsFragment listAllProducts = null")

            val listFilteredProduct = listProduct.filter {
                val productModel = it as ProductModel
                arrayListIdsFiltered.contains(productModel.product_id)
            }

            productsViewModel.setProductsModel(listProductModel = listFilteredProduct)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        navControllerCatalog = findNavController()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.catalog),
            icon = R.drawable.ic_back
        ) { navControllerCatalog.navigateUp()})

        toolbarViewModel.clearMenu()

        val path = arguments?.getString(KEY_PATH)?:
        throw NullPointerException("ProductsFragment path = null")

        val isShown = productsViewModel.isShown.value?:
        throw NullPointerException("ProductsFragment isShown = null")

        if (!isShown) {
            with(productsViewModel) {
                onSuccessfulEvent(type = TYPE_GET_PRODUCTS_BY_PATH) {
                    getProductsByPath(path = path)
                }
            }
        }

        layoutPendingResultProducts.bTryAgain.setOnClickListener {
            with(productsViewModel) {
                onSuccessfulEvent(type = TYPE_GET_PRODUCTS_BY_PATH) {
                    getProductsByPath(path = path)
                }
            }
        }

        bFilters.setOnClickListener {
            val listAllProducts = productsViewModel.listAllProducts.value ?:
            throw NullPointerException("ProductsFragment listAllProducts = null")

            val listPrices = listAllProducts.map {
                val productModel = it as ProductModel

                return@map getPrice(
                    discount = productModel.discount,
                    price = productModel.price
                )
            }

            val defaultPriceFrom = listPrices.min()

            val defaultPriceUpTo = listPrices.max()

            val bundle = Bundle()
            bundle.putString(KEY_PATH, path)

            val isChecked = arguments?.getBoolean(KEY_IS_CHECKED_DISCOUNT) ?: false
            val priceFrom = arguments?.getInt(KEY_PRICE_FROM) ?: -1
            val priceUpTo = arguments?.getInt(KEY_PRICE_UP_TO) ?: -1
            val arrayListIdsSelectedAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()

            Log.i("TAG","ProductsFragment isCheckedFilter = $isChecked")
            Log.i("TAG","ProductsFragment priceFromFilter = $priceFrom")
            Log.i("TAG","ProductsFragment priceUpToFilter = $priceUpTo")
            Log.i("TAG","ProductsFragment defaultPriceFrom = $defaultPriceFrom")
            Log.i("TAG","ProductsFragment defaultPriceUpTo = $defaultPriceUpTo")
            Log.i("TAG","ProductsFragment arrayListIdsSelectedAddressesFilter = $arrayListIdsSelectedAddresses")

            bundle.putBoolean(KEY_IS_CHECKED_DISCOUNT,isChecked)
            bundle.putInt(KEY_PRICE_FROM,priceFrom)
            bundle.putInt(KEY_PRICE_UP_TO,priceUpTo)
            bundle.putInt(KEY_DEFAULT_PRICE_FROM,defaultPriceFrom)
            bundle.putInt(KEY_DEFAULT_PRICE_UP_TO,defaultPriceUpTo)
            bundle.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListIdsSelectedAddresses)

            navControllerCatalog.navigate(R.id.action_productsFragment_to_filterFragment, bundle)
        }

        productsViewModel.result.observe(viewLifecycleOwner) { result ->
            when(result) {
                is PendingResult -> { onPendingResultListener() }
                is SuccessResult -> {
                    val value = result.value
                    onSuccessResultListener(
                        value = value,
                        type = TYPE_GET_PRODUCTS_BY_PATH
                    )
                }
                is ErrorResult -> {
                    val errorType = productsViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }

        productsViewModel.listProducts.observe(viewLifecycleOwner) { listProductsModel ->

            val productsAdapter = ProductsAdapter(listProducts = listProductsModel) { productId ->
                Log.i("TAG","CLICK")
            }

            productsAdapter.notifyItemRangeRemoved(0,listProductsModel.size)

            rvProducts.adapter = productsAdapter
            rvProducts.layoutManager = GridLayoutManager(requireContext(),2)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPendingResultListener() {
        Log.i("TAG","ProductsFragment onPendingResultListener")
        productsViewModel.clearErrorType()
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(value: T, type: String?): Unit {
        Log.i("TAG","ProductsFragment onSuccessResultListener")
        val responseValueModel = value as ResponseValueModel<*>
        val status = responseValueModel.responseModel.status
        val message = responseValueModel.responseModel.message

        val isShown = productsViewModel.isShown.value ?:
        throw NullPointerException("ProductsFragment isShown = null")

        if (!isShown) {
            if (status in 200..299){

                val listProducts = responseValueModel.value as List<*>

                productsViewModel.setProductsModel(listProductModel = listProducts)
                productsViewModel.setListAllProductsModel(listProductModel = listProducts)

                updateUI(flag = FLAG_SUCCESS_RESULT)

            }
            else {
                productsViewModel.setResult(result = ErrorResult(exception = Exception()), errorType = OtherError())
                if (message != null) getSupportActivity().showToast(message = message)
            }
        }

        productsViewModel.setIsShown(isShown = true)
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG","ProductsFragment onErrorResultListener")
        updateUI(FLAG_ERROR_RESULT, messageError = message)

        productsViewModel.setIsShown(isShown = true)
    }

    override fun onSuccessfulEvent(type: String, exception: Exception?, onSuccessfulEventListener:() -> Unit){
        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())
        val network = Network()

        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                when(type) {
                    TYPE_GET_PRODUCTS_BY_PATH -> productsViewModel.setResult(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()
                when(type) {
                    TYPE_GET_PRODUCTS_BY_PATH -> productsViewModel.setResult(result = ErrorResult(exception = currentException), errorType = errorType)
                }
                getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultProducts) {
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

}