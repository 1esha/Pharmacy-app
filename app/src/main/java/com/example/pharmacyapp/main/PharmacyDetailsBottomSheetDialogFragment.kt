package com.example.pharmacyapp.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import coil.load
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.pharmacyapp.FLAG_ALL_PHARMACIES
import com.example.pharmacyapp.FLAG_CURRENT_PRODUCT
import com.example.pharmacyapp.KEY_FLAGS_FOR_MAP
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentPharmacyDetailsBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Класс [PharmacyDetailsBottomSheetDialogFragment] отвечает за появление
 * и отрисовку нижнего всплывающего окна с подробной информацией об текущей аптеке.
 */
class PharmacyDetailsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPharmacyDetailsBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

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

        // Получение флага для отрисовки разметки
        val flag = arguments?.getString(KEY_FLAGS_FOR_MAP) ?: FLAG_ALL_PHARMACIES

        // Получение значения в наличии товар или нет
        val isAvailability = arguments?.getBoolean(KEY_IS_AVAILABILITY)

        // Получение подробной информации о аптеке
        val pharmacyAddressesDetails: PharmacyAddressesDetailsModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(KEY_PHARMACY_ADDRESSES_DETAILS, PharmacyAddressesDetailsModel::class.java)
        }
        else {
            arguments?.getSerializable(KEY_PHARMACY_ADDRESSES_DETAILS) as PharmacyAddressesDetailsModel
        } ?: throw NullPointerException("PharmacyDetailsBottomSheetDialogFragment pharmacyAddressesDetails = null")

        // получение списков с графиками работы текущей аптеки
        val arrayListOperatingModesTimeFrom = arguments?.getStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM)?:
        throw NullPointerException("PharmacyDetailsBottomSheetDialogFragment arrayListOperatingModesTimeFrom = null")

        val arrayListOperatingModesTimeBefore = arguments?.getStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE)?:
        throw NullPointerException("PharmacyDetailsBottomSheetDialogFragment arrayListOperatingModesTimeBefore = null")

        // Установка изображения аптеки
        ivPharmacy.load(pharmacyAddressesDetails.image)

        // Установка адреса аптеки
        tvAddressPharmacyForMap.text = pharmacyAddressesDetails.pharmacyAddressesModel.address
        tvCityAddressPharmacyForMap.text = pharmacyAddressesDetails.pharmacyAddressesModel.city

        // Заолнение режима работы
        fillingOperatingMode(
            arrayListOperatingModesTimeFrom = arrayListOperatingModesTimeFrom,
            arrayListOperatingModesTimeBefore = arrayListOperatingModesTimeBefore
        )

        // Проверка как сейчас надо отрисовывать разметку
        when (flag) {
            FLAG_CURRENT_PRODUCT -> {

                layoutAvailabilityInPharmacy.visibility = View.VISIBLE

                when(isAvailability) {
                    true -> {
                        val color = ContextCompat.getColor(requireContext(), R.color.green800)

                        tvAvailabilityStatus.setTextColor(color)
                        tvAvailabilityStatus.text = getString(R.string.in_stock)
                        ivAvailabilityStatus.setImageResource(R.drawable.ic_check_circle)
                    }

                    false -> {
                        val color = ContextCompat.getColor(requireContext(), R.color.discount)

                        tvAvailabilityStatus.setTextColor(color)
                        tvAvailabilityStatus.text = getString(R.string.out_of_stock)
                        ivAvailabilityStatus.setImageResource(R.drawable.ic_remove_circle)
                    }

                    else -> throw IllegalArgumentException()
                }

            }
            FLAG_ALL_PHARMACIES -> {
                layoutAvailabilityInPharmacy.visibility = View.GONE
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Заполнение режима работы, выбранной аптеки.
     *
     * Параметры:
     * [arrayListOperatingModesTimeFrom] - список с временем начала работы;
     * [arrayListOperatingModesTimeBefore] - список с временем окончания работы.
     */
    private fun fillingOperatingMode(
        arrayListOperatingModesTimeFrom: ArrayList<String>,
        arrayListOperatingModesTimeBefore: ArrayList<String>
        ) = with(binding) {

            for (day in 1..7) {

                val index = day-1

                val textTimeFrom = arrayListOperatingModesTimeFrom[index].toHourAndMinutes()
                val textTimeBefore = arrayListOperatingModesTimeBefore[index].toHourAndMinutes()

                val fullTime = "$textTimeFrom-$textTimeBefore"

                when(day) {
                    1 -> tvMo.text = fullTime
                    2 -> tvTu.text = fullTime
                    3 -> tvWe.text = fullTime
                    4 -> tvTh.text = fullTime
                    5 -> tvFr.text = fullTime
                    6 -> tvSa.text = fullTime
                    7 -> tvSu.text = fullTime
                }

            }
    }

    /**
     * Преобразование сторки времени к формату ЧЧ:ММ.
     */
    private fun String.toHourAndMinutes(): String{
        var time = ""
        var counter = 0
        this.forEach {
            if (it == ':') counter++
            if (counter <= 1) time += it
        }
        return time
    }

    companion object {

        // Тэг нимжнего всплывающего окна
        const val TAG_PHARMACY_DETAILS_BOTTOM_SHEET = "TAG_PHARMACY_DETAILS_BOTTOM_SHEET"

        // Ключи для передачи и получения аргуметов
        const val KEY_PHARMACY_ADDRESSES_DETAILS = "KEY_PHARMACY_ADDRESSES_DETAILS"
        const val KEY_IS_AVAILABILITY = "KEY_IS_AVAILABILITY"
        const val KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM = "KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM"
        const val KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE = "KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE"

        fun newInstance(
            pharmacyAddressesDetailsModel: PharmacyAddressesDetailsModel,
            arrayListOperatingModesTimeFrom: ArrayList<String>,
            arrayListOperatingModesTimeBefore: ArrayList<String>,
            isAvailability: Boolean,
            flag: String
        ): PharmacyDetailsBottomSheetDialogFragment {
            val args = Bundle()
            args.apply {
                putSerializable(KEY_PHARMACY_ADDRESSES_DETAILS,pharmacyAddressesDetailsModel)
                putStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_FROM,arrayListOperatingModesTimeFrom)
                putStringArrayList(KEY_ARRAY_LIST_OPERATING_MODES_TIME_BEFORE,arrayListOperatingModesTimeBefore)
                putBoolean(KEY_IS_AVAILABILITY,isAvailability)
                putString(KEY_FLAGS_FOR_MAP,flag)
            }

            val dialog = PharmacyDetailsBottomSheetDialogFragment()
            dialog.arguments = args

            return dialog
        }
    }
}