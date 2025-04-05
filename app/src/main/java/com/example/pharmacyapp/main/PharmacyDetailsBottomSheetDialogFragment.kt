package com.example.pharmacyapp.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import coil.load
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.pharmacyapp.KEY_FLAGS_FOR_MAP
import com.example.pharmacyapp.databinding.FragmentPharmacyDetailsBottomSheetDialogBinding
import com.example.pharmacyapp.main.viewmodels.PharmacyDetailsBottomSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Класс [PharmacyDetailsBottomSheetDialogFragment] отвечает за появление
 * и отрисовку нижнего всплывающего окна с подробной информацией об текущей аптеке.
 */
class PharmacyDetailsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPharmacyDetailsBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    private val pharmacyDetailsBottomSheetViewModel: PharmacyDetailsBottomSheetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPharmacyDetailsBottomSheetDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){

        // Получение подробной информации о аптеке
        val pharmacyAddressesDetails: PharmacyAddressesDetailsModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(KEY_PHARMACY_ADDRESSES_DETAILS, PharmacyAddressesDetailsModel::class.java)
        }
        else {
            arguments?.getSerializable(KEY_PHARMACY_ADDRESSES_DETAILS) as PharmacyAddressesDetailsModel
        } ?: throw NullPointerException("PharmacyDetailsBottomSheetDialogFragment pharmacyAddressesDetails = null")


        pharmacyDetailsBottomSheetViewModel.initValues(
            flag = arguments?.getString(KEY_FLAGS_FOR_MAP),
            flagStatus = arguments?.getInt(KEY_FLAG_STATUS_NUMBER_PRODUCT),
            pharmacyAddressesDetails = pharmacyAddressesDetails,
            arrayListOperatingModesTimeFrom = arguments?.getStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM),
            arrayListOperatingModesTimeBefore = arguments?.getStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE),
            availableQuantity = arguments?.getInt(KEY_AVAILABLE_QUANTITY),
            totalNumber = arguments?.getInt(KEY_TOTAL_NUMBER)
        )

        // Установка изображения аптеки
        ivPharmacy.load(pharmacyAddressesDetails.image)

        // Установка адреса аптеки
        tvAddressPharmacyForMap.text = pharmacyAddressesDetails.pharmacyAddressesModel.address
        tvCityAddressPharmacyForMap.text = pharmacyAddressesDetails.pharmacyAddressesModel.city

        // Заолнение режима работы
        fillingOperatingMode()

        pharmacyDetailsBottomSheetViewModel.installMap { isVisibilityLayoutStatus, isVisibilityButton, isEnabledButton ->
            layoutAvailabilityInPharmacy.visibility = if (isVisibilityLayoutStatus) View.VISIBLE else View.GONE
            bChooseAddressOnMap.visibility = if (isVisibilityButton) View.VISIBLE else View.GONE
            bChooseAddressOnMap.isEnabled = isEnabledButton
        }

        installUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        dismiss()
    }

    private fun installUI() = with(binding){
        pharmacyDetailsBottomSheetViewModel.installUI { colorInt,textInt,image,availableQuantity,totalNumber ->
            val color = ContextCompat.getColor(requireContext(), colorInt)

            val text = if (availableQuantity in 1..< totalNumber){
                getString(textInt)+" $availableQuantity / $totalNumber"
            } else getString(textInt)

            tvAvailabilityStatus.setTextColor(color)
            tvAvailabilityStatus.text = text
            ivAvailabilityStatus.setImageResource(image)
        }
    }

    private fun fillingOperatingMode() = with(binding) {
        pharmacyDetailsBottomSheetViewModel.fillingOperatingMode { textMo,textTu,textWe,textTh,textFr,textSa,textSu ->
            tvMo.text = textMo
            tvTu.text = textTu
            tvWe.text = textWe
            tvTh.text = textTh
            tvFr.text = textFr
            tvSa.text = textSa
            tvSu.text = textSu
        }
    }

    companion object {

        // Тэг нимжнего всплывающего окна
        const val TAG_PHARMACY_DETAILS_BOTTOM_SHEET = "TAG_PHARMACY_DETAILS_BOTTOM_SHEET"

        // Ключи для передачи и получения аргуметов
        const val KEY_PHARMACY_ADDRESSES_DETAILS = "KEY_PHARMACY_ADDRESSES_DETAILS"
        const val KEY_AVAILABLE_QUANTITY = "KEY_AVAILABLE_QUANTITY"
        const val KEY_TOTAL_NUMBER = "KEY_TOTAL_NUMBER"
        const val KEY_FLAG_STATUS_NUMBER_PRODUCT = "KEY_FLAG_STATUS_NUMBER_PRODUCT"
        const val KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM = "KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM"
        const val KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE = "KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE"

        fun newInstance(
            pharmacyAddressesDetailsModel: PharmacyAddressesDetailsModel,
            arrayListOperatingModesTimeFrom: ArrayList<String>,
            arrayListOperatingModesTimeBefore: ArrayList<String>,
            flagStatus: Int,
            availableQuantity: Int? = null,
            totalNumber: Int? = null,
            flag: String
        ): PharmacyDetailsBottomSheetDialogFragment {
            val args = Bundle()
            args.apply {
                putSerializable(KEY_PHARMACY_ADDRESSES_DETAILS,pharmacyAddressesDetailsModel)
                putStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM,arrayListOperatingModesTimeFrom)
                putStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE,arrayListOperatingModesTimeBefore)
                putInt(KEY_FLAG_STATUS_NUMBER_PRODUCT,flagStatus)

                if (availableQuantity != null && totalNumber != null){
                    putInt(KEY_AVAILABLE_QUANTITY,availableQuantity)
                    putInt(KEY_TOTAL_NUMBER,totalNumber)
                }

                putString(KEY_FLAGS_FOR_MAP,flag)
            }

            val dialog = PharmacyDetailsBottomSheetDialogFragment()
            dialog.arguments = args

            return dialog
        }
    }
}