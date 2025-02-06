package com.example.pharmacyapp.tabs.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentSortingBottomSheetDialogBinding
import com.example.pharmacyapp.getSupportActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SortingBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentSortingBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSortingBottomSheetDialogBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){

        val position = arguments?.getInt(KEY_SORTING_TYPE) ?: SORT_ASCENDING_PRICE

        when(position) {
            SORT_ASCENDING_PRICE -> {
                radioGroupSort.check(R.id.bAscendingPrice)
            }
            SORT_DESCENDING_PRICE -> {
                radioGroupSort.check(R.id.bDescendingPrice)
            }
            SORT_DISCOUNT_AMOUNT -> {
                radioGroupSort.check(R.id.bDiscountAmount)
            }
        }

        radioGroupSort.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->

            val newPosition: Int = when(checkedId) {

                R.id.bDescendingPrice -> SORT_DESCENDING_PRICE

                R.id.bDiscountAmount -> SORT_DISCOUNT_AMOUNT

                else -> SORT_ASCENDING_PRICE
            }

            arguments?.putInt(KEY_SORTING_TYPE,newPosition)

        })

        bShowSortedProducts.setOnClickListener {
            val selectedPosition = arguments?.getInt(KEY_SORTING_TYPE) ?: SORT_ASCENDING_PRICE
            val result = Bundle()
            result.putInt(KEY_SORTING_TYPE,selectedPosition)
            getSupportActivity().setFragmentResult(KEY_RESULT_SORTING_PRODUCTS, result)
            dismiss()
        }

    }

    companion object {

        const val KEY_RESULT_SORTING_PRODUCTS = "KEY_RESULT_SORTING_PRODUCTS"
        const val KEY_SORTING_TYPE = "KEY_SORTING_TYPE"
        const val SORT_ASCENDING_PRICE = 1
        const val SORT_DESCENDING_PRICE = 2
        const val SORT_DISCOUNT_AMOUNT = 3
        const val TAG_SORTING_BOTTOM_SHEET = "TAG_SORTING_BOTTOM_SHEET"

        fun newInstance(type: Int): SortingBottomSheetDialogFragment {
            val args = Bundle()
            args.apply {
                putInt(KEY_SORTING_TYPE,type)
            }
            val fragment = SortingBottomSheetDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }
}