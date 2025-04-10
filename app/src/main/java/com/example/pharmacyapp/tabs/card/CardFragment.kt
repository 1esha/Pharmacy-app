package com.example.pharmacyapp.tabs.card

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
import androidx.navigation.navOptions
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.KEY_USER_NUMBER_PHONE
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentCardBinding
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.card.viewmodels.CardViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch


class CardFragment : Fragment() {

    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val cardViewModel: CardViewModel by viewModels()

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                cardViewModel.isAuthorizedUser.collect{ isAuthorizedUser ->
                    installUI(isAuthorizedUser = isAuthorizedUser)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerMain = getSupportActivity().getNavControllerMain()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.my_card)){})
        toolbarViewModel.clearMenu()

        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)
        val numberPhone = sharedPreferences.getString(KEY_USER_NUMBER_PHONE,null)

        cardViewModel.installQRCode(
            userId = userId,
            numberPhone = numberPhone
        ) { content ->

            val barcodeEncoder = BarcodeEncoder()
            val newBitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400);

            ivQRCodeClubDiscount.setImageBitmap(newBitmap)
        }

        bGoToLogInForCard.setOnClickListener {
            navControllerMain.navigate(R.id.nav_graph_log_in, null, navOptions {
                popUpTo(R.id.tabsFragment){
                    inclusive = true
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun installUI(isAuthorizedUser: Boolean?) = with(binding){
        when(isAuthorizedUser){
            null -> {
                layoutAuthorizedUserForCard.visibility = View.GONE
                layoutUnauthorizedUserForCard.visibility = View.GONE
            }
            true -> {
                layoutAuthorizedUserForCard.visibility = View.VISIBLE
                layoutUnauthorizedUserForCard.visibility = View.GONE
            }
            false -> {
                layoutAuthorizedUserForCard.visibility = View.GONE
                layoutUnauthorizedUserForCard.visibility = View.VISIBLE
            }
        }
    }

}