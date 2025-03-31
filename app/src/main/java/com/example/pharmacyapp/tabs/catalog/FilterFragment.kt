package com.example.pharmacyapp.tabs.catalog


import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.RequestModel
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
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentFilterBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.FilterViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.FilterViewModelFactory
import kotlinx.coroutines.launch
import java.lang.Exception

class FilterFragment : Fragment(), ResultProcessing, View.OnKeyListener {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val filterViewModel: FilterViewModel by viewModels(
        factoryProducer = { FilterViewModelFactory() }
    )

    private lateinit var navControllerCatalog: NavController

    private val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(filterViewModel) {

            navControllerCatalog = findNavController()

            initValues(
                isChecked = arguments?.getBoolean(KEY_IS_CHECKED_DISCOUNT),
                arrayListIdsSelectedAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES),
                priceFrom = arguments?.getInt(KEY_PRICE_FROM),
                priceUpTo = arguments?.getInt(KEY_PRICE_UP_TO),
                defaultPriceFrom = arguments?.getInt(KEY_DEFAULT_PRICE_FROM),
                defaultPriceUpTo = arguments?.getInt(KEY_DEFAULT_PRICE_UP_TO),
                path = arguments?.getString(KEY_PATH)
            )

            setFragmentResultListener(KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES) { requestKey, bundle ->
                listenResultFromPharmacyAddresses(
                    newArrayListIdsSelectedAddresses = bundle.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES)
                )
            }

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
                    arrayListIdsSelectedAddresses.collect { arrayListIdsSelectedAddresses ->
                        installSelectedAddresses(arrayListIdsSelectedAddresses = arrayListIdsSelectedAddresses)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    isChecked.collect { isChecked ->
                        binding.checkBoxDiscountedProduct.isChecked = isChecked
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    priceFrom.collect { priceFrom ->
                        val textPriceFrom = priceFrom.toString()
                        binding.etFrom.setText(textPriceFrom)
                        filterViewModel.checkCorrectPriceFrom(textPriceFrom = textPriceFrom)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    priceUpTo.collect { priceUpTo ->
                        val textPriceUpTo = priceUpTo.toString()
                        binding.etUpTo.setText(textPriceUpTo)
                        filterViewModel.checkCorrectPriceUpTo(
                            textPriceUpTo = textPriceUpTo,
                            priceFrom = binding.etFrom.text.toString().toInt()
                        )
                    }
                }
            }
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

        etFrom.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                val priceFrom = binding.etFrom.text.toString().toInt()
                filterViewModel.setPriceFrom(priceFrom = priceFrom)
            }
        }

        etUpTo.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus){
                val priceUpTo = binding.etUpTo.text.toString().toInt()
                filterViewModel.setPriceUpTo(priceUpTo = priceUpTo)
            }
        }

        checkBoxDiscountedProduct.setOnCheckedChangeListener { _, isChecked ->
            filterViewModel.setChecked(isChecked = isChecked)
        }

        layoutAvailabilityInPharmacies.setOnClickListener {
            filterViewModel.navigateToPharmacyAddresses { path, arrayListIdsSelectedAddresses ->
                val bundle = Bundle().apply {
                    putString(KEY_PATH, path)
                    putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES, arrayListIdsSelectedAddresses)
                }

                navControllerCatalog.navigate(R.id.action_filterFragment_to_pharmacyAddressesFragment, bundle)
            }
        }

        bShowFilteredProducts.setOnClickListener {
            filterViewModel.backToProducts { isChecked, priceFrom, priceUpTo,arrayListIdsSelectedAddresses,arrayListIdsFilteredProducts ->
                Log.d("TAG","FilterFragment\nisChecked = $isChecked\npriceFrom = $priceFrom\npriceUpTo = $priceUpTo\narrayListIdsSelectedAddresses = $arrayListIdsSelectedAddresses\narrayListIdsFilteredProducts = $arrayListIdsFilteredProducts")
                val bundle = Bundle().apply {
                    putIntegerArrayList(KEY_ARRAY_LIST_IDS_FILTERED, arrayListIdsFilteredProducts)
                    putBoolean(KEY_IS_CHECKED_DISCOUNT, isChecked)
                    putInt(KEY_PRICE_FROM, priceFrom)
                    putInt(KEY_PRICE_UP_TO, priceUpTo)
                    putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES, arrayListIdsSelectedAddresses)
                }

                getSupportActivity().setFragmentResult(KEY_RESULT_ARRAY_LIST_IDS_FILTERED, bundle)

                navControllerCatalog.popBackStack()
            }
        }

        layoutPendingResultFilter.bTryAgain.setOnClickListener {
            filterViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        etUpTo.setOnKeyListener(this@FilterFragment)

        etFrom.setOnKeyListener(this@FilterFragment)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onKey(view: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyEvent?.action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                filterViewModel.setPriceFrom(priceFrom = binding.etFrom.text.toString().toInt())
                filterViewModel.setPriceUpTo(priceUpTo = binding.etUpTo.text.toString().toInt())
            }
        }
        return false
    }

    override fun onLoadingResultListener() {
        Log.i("TAG", "FilterFragment onLoadingResultListener")
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG", "FilterFragment onSuccessResultListener")
        try {
            installToolbar()

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
                TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH + TYPE_GET_PRODUCTS_BY_PATH -> {
                    Log.i("TAG", "fullType =  TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH + TYPE_GET_PRODUCTS_BY_PATH")
                    val resultGetProductAvailabilityByPath = listRequests.find { it.type == TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH }?.result!!.asSuccess()!!
                    val resultGetProductsByPath = listRequests.find { it.type == TYPE_GET_PRODUCTS_BY_PATH }?.result!!.asSuccess()!!

                    val responseGetProductAvailabilityByPath = resultGetProductAvailabilityByPath.data as ResponseValueModel<*>
                    val responseGetProductsByPath = resultGetProductsByPath.data as ResponseValueModel<*>

                    val _listProductAvailability = responseGetProductAvailabilityByPath.value as List<*>
                    val listProductAvailability = _listProductAvailability.map { it as ProductAvailabilityModel }

                    val _listAllProducts = responseGetProductsByPath.value as List<*>
                    val listAllProducts = _listAllProducts.map { it as ProductModel }

                    filterViewModel.fillData(
                        listProductAvailability = listProductAvailability,
                        listAllProducts = listAllProducts
                    )
                }

            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        } catch (e: Exception) {
            Log.e("TAG", e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        Log.i("TAG", "FilterFragment onErrorResultListener")
        toolbarViewModel.clearMenu()

        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun updateUI(flag: String, messageError: String?) =
        with(binding.layoutPendingResultFilter) {
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

    private fun installToolbar() = with(toolbarViewModel){
        installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.filters),
            icon = R.drawable.ic_back
        ) {
            navControllerCatalog.navigateUp()
        })

        clearMenu()

        inflateMenu(menu = R.menu.menu_delete_filters)
        setMenuClickListener { menuItemId ->
            if (menuItemId == R.id.deleteFilters) {
                filterViewModel.clearFilters()
            }
        }
    }

    private fun installSelectedAddresses(arrayListIdsSelectedAddresses: ArrayList<Int>) = with(binding){
        val numberSelectedAddresses = arrayListIdsSelectedAddresses.size

        if (numberSelectedAddresses > 0) {
            layoutSelectedAddresses.visibility = View.VISIBLE
            val textNumberSelectedAddresses = numberSelectedAddresses.toString()
            tvNumberSelectedAddresses.text = textNumberSelectedAddresses
        } else {
            layoutSelectedAddresses.visibility = View.GONE
        }
    }
}