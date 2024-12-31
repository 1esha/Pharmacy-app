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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.SuccessResult
import com.example.domain.catalog.CatalogResult
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESUL
import com.example.pharmacyapp.FLAG_PENDING_RESUL
import com.example.pharmacyapp.FLAG_SUCCESS_RESUL
import com.example.pharmacyapp.KEY_PATH
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.ToolbarDataModel
import com.example.pharmacyapp.databinding.FragmentProductsBinding
import com.example.pharmacyapp.getMessageByErrorType
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

    private lateinit var navControllerProducts: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        navControllerProducts = findNavController()

        toolbarViewModel.setToolbarData(toolbarDataModel = ToolbarDataModel(
            title = getString(R.string.catalog),
            icon = R.drawable.ic_back
        ) { navControllerProducts.navigateUp()})

        val path = arguments?.getString(KEY_PATH)?: throw NullPointerException("ProductsFragment path = null")
        val isShown = productsViewModel.isShown.value?: throw NullPointerException("ProductsFragment isShown = null")
        if (!isShown) {
            with(productsViewModel) {
                onSuccessfulEvent(type = TYPE_GET_PRODUCTS_BY_PATH) {
                    getProductsByPath(path = path)
                }
            }
        }

        layoutPendingResultProducts.bTryAgain.setOnClickListener {
            with(productsViewModel) {
                setIsShown(isShown = false)
                onSuccessfulEvent(type = TYPE_GET_PRODUCTS_BY_PATH) {
                    getProductsByPath(path = path)
                }
            }
        }

        productsViewModel.result.observe(viewLifecycleOwner) { result ->
            when(result) {
                is PendingResult -> { onPendingResult() }
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPendingResult() {
        Log.i("TAG","ProductsFragment onPendingResult")
        productsViewModel.clearErrorType()
        updateUI(flag = FLAG_PENDING_RESUL)
    }

    override fun <T> onSuccessResultListener(value: T, type: String?): Unit = with(binding){
        Log.i("TAG","ProductsFragment onSuccessResultListener")
        val responseValueModel = value as ResponseValueModel<*>
        val status = responseValueModel.responseModel.status
        val message = responseValueModel.responseModel.message

        if (status in 200..299){
            updateUI(flag = FLAG_SUCCESS_RESUL)

            val listProducts = responseValueModel.value as List<*>

            val productsAdapter = ProductsAdapter(listProducts = listProducts) { productId ->
                Log.i("TAG","CLICK")
            }
            Log.i("TAG","CHECK $listProducts")
            rvProducts.adapter = productsAdapter
            rvProducts.layoutManager = GridLayoutManager(requireContext(),2)

        }
        else {
            productsViewModel.setResult(result = ErrorResult(exception = Exception()), errorType = OtherError())
            if (message != null) getSupportActivity().showToast(message = message)
        }

        productsViewModel.setIsShown(isShown = true)
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG","ProductsFragment onErrorResultListener")
        updateUI(FLAG_ERROR_RESUL, messageError = message)
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

    private fun updateUI(flag: String, messageError: String? = null) = with(binding.layoutPendingResultProducts) {
        when(flag) {
            FLAG_PENDING_RESUL -> {
                root.visibility = View.VISIBLE
                bTryAgain.visibility = View.INVISIBLE
                tvErrorMessage.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }
            FLAG_SUCCESS_RESUL -> {
                root.visibility = View.GONE
                bTryAgain.visibility = View.INVISIBLE
                tvErrorMessage.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
            }
            FLAG_ERROR_RESUL -> {
                root.visibility = View.VISIBLE
                bTryAgain.visibility = View.VISIBLE
                tvErrorMessage.visibility = View.VISIBLE
                tvErrorMessage.text = messageError
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

}