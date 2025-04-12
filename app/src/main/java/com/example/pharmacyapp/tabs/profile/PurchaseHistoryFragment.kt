package com.example.pharmacyapp.tabs.profile

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.OrderProductModel
import com.example.domain.models.RequestModel
import com.example.domain.orders.models.OrderModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_EMPTY_RESULT
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_IDS
import com.example.pharmacyapp.TYPE_GET_PURCHASE_HISTORY
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentPurchaseHistoryBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.profile.adapters.PurchaseHistoryAdapter
import com.example.pharmacyapp.tabs.profile.viewmodels.PurchaseHistoryViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.factories.PurchaseHistoryViewModelFactory
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Класс [PurchaseHistoryFragment] является экраном со списком всех покупок.
 */
class PurchaseHistoryFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentPurchaseHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var navControllerProfile: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val purchaseHistoryViewModel: PurchaseHistoryViewModel by viewModels(
        factoryProducer = { PurchaseHistoryViewModelFactory() }
    )

    private lateinit var purchaseHistoryAdapter: PurchaseHistoryAdapter

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        purchaseHistoryViewModel.initValues(
            userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)
        )

        purchaseHistoryViewModel.sendingRequests(isNetworkStatus = isNetworkStatus)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                purchaseHistoryViewModel.stateScreen.collect{ result ->
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
                purchaseHistoryViewModel.listOrderProductModel.collect{ listOrderProductModel ->
                    installUI(listOrderProductModel = listOrderProductModel)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurchaseHistoryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        navControllerProfile = findNavController()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.purchase_history),
            icon = R.drawable.ic_back
        ) {
            navControllerProfile.navigateUp()
        })
        toolbarViewModel.clearMenu()

        layoutPendingResultPurchaseHistory.bTryAgain.setOnClickListener {
            purchaseHistoryViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","PurchaseHistoryFragment onSuccessResultListener")
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
                TYPE_GET_PRODUCTS_BY_IDS + TYPE_GET_PURCHASE_HISTORY -> {
                    val resultGetProductsByIds = listRequests.find { it.type == TYPE_GET_PRODUCTS_BY_IDS }?.result!!.asSuccess()!!
                    val resultPurchaseHistory = listRequests.find { it.type == TYPE_GET_PURCHASE_HISTORY }?.result!!.asSuccess()!!

                    val responseGetProductsByIds = resultGetProductsByIds.data as ResponseValueModel<*>
                    val responsePurchaseHistory = resultPurchaseHistory.data as ResponseValueModel<*>

                    val _listProductModel = responseGetProductsByIds.value as List<*>
                    val listProductModel  = _listProductModel.map { it as ProductModel }

                    val _listOrderModel = responsePurchaseHistory.value as List<*>
                    val listOrderModel  = _listOrderModel.map { it as OrderModel }

                    purchaseHistoryViewModel.fillData(
                        listProductModel = listProductModel,
                        listOrderModel = listOrderModel
                    )
                }
                TYPE_EMPTY_RESULT -> {
                    purchaseHistoryViewModel.installEmptyList()
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        Log.i("TAG","PurchaseHistoryFragment onErrorResultListener")
        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        Log.i("TAG","PurchaseHistoryFragment onLoadingResultListener")
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultPurchaseHistory){
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

    private fun installUI(listOrderProductModel: List<OrderProductModel>) = with(binding){
        if (listOrderProductModel.isEmpty()){
            tvEmptyListPurchaseHistory.visibility = View.VISIBLE
            rvPurchaseHistory.visibility = View.GONE
        }
        else{
            tvEmptyListPurchaseHistory.visibility = View.GONE
            rvPurchaseHistory.visibility = View.VISIBLE

            purchaseHistoryAdapter = PurchaseHistoryAdapter(
                listOrderProductModel = listOrderProductModel
            )
            rvPurchaseHistory.adapter = purchaseHistoryAdapter
            rvPurchaseHistory.layoutManager = LinearLayoutManager(requireContext())
        }
    }
}