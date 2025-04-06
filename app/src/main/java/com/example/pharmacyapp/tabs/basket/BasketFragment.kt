package com.example.pharmacyapp.tabs.basket

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.basket.models.BasketModel
import com.example.domain.models.RequestModel
import com.example.domain.models.SelectedBasketModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP
import com.example.pharmacyapp.KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_DELETE_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_UPDATE_NUMBER_PRODUCTS_IN_BASKET
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentBasketBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.basket.adapters.BasketAdapter
import com.example.pharmacyapp.tabs.basket.viewmodels.BasketViewModel
import com.example.pharmacyapp.tabs.basket.viewmodels.factories.BasketViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class BasketFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentBasketBinding? = null
    private val binding get() = _binding!!

    private val basketViewModel: BasketViewModel by viewModels(
        factoryProducer = { BasketViewModelFactory() }
    )

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var navControllerBasket: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var basketAdapter: BasketAdapter

    private val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        basketViewModel.initValues(userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER))

        navControllerBasket = findNavController()

        basketViewModel.sendingRequests(isNetworkStatus = isNetworkStatus)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                basketViewModel.stateScreen.collect{ result ->
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
                basketViewModel.listSelectedBasketModel.collect{ listSelectedBasketModel ->
                    installUI(mutableListSelectedBasketModel = listSelectedBasketModel.toMutableList())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasketBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.basket)){})
        toolbarViewModel.clearMenu()

        layoutPendingResultBasket.bTryAgain.setOnClickListener {
            basketViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        checkBoxSelectAllProductInBasket.setOnClickListener {
            try {
                val isSelect = checkBoxSelectAllProductInBasket.isChecked
                basketViewModel.onSelectAll(isSelect = isSelect)
                basketAdapter.selectAll(isSelect = isSelect)
            }
            catch (e: Exception){
                Log.e("TAG",e.stackTraceToString())
            }
        }

        val listenerDialogDeleteProducts = DialogInterface.OnClickListener { dialog, button ->
            when(button){
                DialogInterface.BUTTON_POSITIVE -> {
                    try {
                        onDeleteProductsFromBasket()
                    }
                    catch (e: Exception){
                        Log.e("TAG",e.stackTraceToString())
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }
            }
        }

        val dialogDeleteProducts = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.removing_items_from_the_shopping_cart)
            .setMessage(R.string.are_you_sure_you_want_to_delete_the_selected_products_it_will_be_impossible_to_cancel_this_action)
            .setPositiveButton(R.string.delete,listenerDialogDeleteProducts)
            .setNegativeButton(R.string.cancel,listenerDialogDeleteProducts)
            .create()

        bDeleteSelectedProductsFromBasket.setOnClickListener {
            dialogDeleteProducts.show()
        }

        bGoToOrderMaking.setOnClickListener {
            basketViewModel.navigateToOrderMaking { arrayListIdsSelectedBasketModel, arrayListNumberProductsSelectedBasketModels ->
                val bundle = Bundle().apply {
                    putIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP,arrayListIdsSelectedBasketModel)
                    putIntegerArrayList(KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP,arrayListNumberProductsSelectedBasketModels)
                }
                navControllerBasket.navigate(R.id.action_basketFragment_to_chooseAddressForOrderMakingFragment, bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        basketViewModel.setIsInstallAdapter(isInstallAdapter = true)
    }

    override fun <T> onSuccessResultListener(data: T) {
        try {
            Log.i("TAG","onSuccessResultListener")
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
                TYPE_GET_PRODUCTS_FROM_BASKET -> {
                    Log.i("TAG","fullType = TYPE_GET_PRODUCTS_FROM_BASKET")
                    val result = listRequests.find { it.type == TYPE_GET_PRODUCTS_FROM_BASKET }?.result!!.asSuccess()!!

                    val responseValueModel = result.data as ResponseValueModel<*>
                    val _listSelectedBasketModel = responseValueModel.value as List<*>
                    val listSelectedBasketModel = _listSelectedBasketModel.map {
                        return@map it as BasketModel
                    }

                    basketViewModel.fillData(listSelectedBasketModel)
                }
                TYPE_DELETE_PRODUCTS_FROM_BASKET -> {
                    Log.i("TAG","fullType = TYPE_DELETE_PRODUCTS_FROM_BASKET")

                    basketViewModel.removeProductsByIds()
                }
                TYPE_UPDATE_NUMBER_PRODUCTS_IN_BASKET -> {
                    Log.i("TAG","fullType = TYPE_UPDATE_NUMBER_PRODUCTS_IN_BASKET")

                    basketViewModel.changeListSelectedBasket()
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultBasket) {
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

    private fun installUI(mutableListSelectedBasketModel: MutableList<SelectedBasketModel>) = with(binding){
        basketViewModel.installUI(mutableListSelectedBasketModel = mutableListSelectedBasketModel) { isEmpty, isAllSelect, isLeastOneSelected ->
            if (isEmpty){
                tvEmptyListBasket.visibility =  View.VISIBLE
                layoutControlPanelBasket.visibility = View.GONE
            }
            else{
                tvEmptyListBasket.visibility =  View.GONE
                layoutControlPanelBasket.visibility = View.VISIBLE

                checkBoxSelectAllProductInBasket.isChecked = isAllSelect

                bDeleteSelectedProductsFromBasket.visibility = if (isLeastOneSelected) View.VISIBLE else View.INVISIBLE

                bGoToOrderMaking.visibility = if (isLeastOneSelected) View.VISIBLE else View.GONE
            }

            basketViewModel.installAdapter {
                basketAdapter = BasketAdapter(
                    mutableListSelectedBasket = mutableListSelectedBasketModel,
                    onClickCheckBox = ::onClickCheckBox,
                    onUpdateNumberProducts = ::onUpdateNumberProduct
                )

                rvBasket.adapter = basketAdapter
                rvBasket.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun onClickCheckBox(newSelectedBasketModel: SelectedBasketModel){
        basketViewModel.onClickCheckBox(newSelectedBasketModel)
    }

    private fun onUpdateNumberProduct(newSelectedBasketModel: SelectedBasketModel){
        basketViewModel.onUpdateNumberProduct(
            isNetworkStatus = isNetworkStatus,
            newSelectedBasketModel = newSelectedBasketModel
        )
    }

    private fun onDeleteProductsFromBasket() {
        basketViewModel.onDeleteProductsFromBasket(isNetworkStatus = isNetworkStatus)
    }
}