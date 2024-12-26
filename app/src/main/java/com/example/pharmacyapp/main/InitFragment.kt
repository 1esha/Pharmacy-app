package com.example.pharmacyapp.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.viewpager2.widget.ViewPager2
import com.example.domain.models.PageModel
import com.example.pharmacyapp.KEY_IS_INIT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentInitBinding
import com.example.pharmacyapp.main.adapters.InitAdapter

class InitFragment : Fragment() {

    private var _binding: FragmentInitBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navControllerMain: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        navControllerMain = findNavController()

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        val listPages = listOf(
            PageModel(getString(R.string.book_products_at_pharmacies_at_a_bargain_price), 0),
            PageModel(getString(R.string.get_discounts), 0),
            PageModel(getString(R.string.log_in_to_get_more_benefits), 0)
        )
        val initAdapter = InitAdapter(
            listPages = listPages,
            onClickGoToLogIn = ::onClickGoToLogIn
        )
        vpInit.adapter = initAdapter
        vpInit.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updatePageCircle(position)
            }

        })

        bSkip.setOnClickListener {
            onClickSkip()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickGoToLogIn() {
        val navControllerMain = findNavController()
        navControllerMain.navigate(R.id.nav_graph_log_in, null, navOptions {
            popUpTo(R.id.initFragment){
                inclusive = true
            }
        })

    }

    private fun onClickSkip(){
        sharedPreferences.apply {
            edit().putBoolean(KEY_IS_INIT,false).apply()
            edit().putInt(KEY_USER_ID, UNAUTHORIZED_USER).apply()
        }
        navControllerMain.navigate(R.id.action_initFragment_to_tabsFragment, null, navOptions {
            popUpTo(R.id.initFragment){
                inclusive = true
            }
        })
    }

    private fun updatePageCircle(selectedPosition: Int) = with(binding) {
        when (selectedPosition) {
            0 -> {
                ivCircle1.setImageResource(R.drawable.shape_circle_selected)
                ivCircle2.setImageResource(R.drawable.shape_circle_not_selected)
                ivCircle3.setImageResource(R.drawable.shape_circle_not_selected)
            }

            1 -> {
                ivCircle1.setImageResource(R.drawable.shape_circle_not_selected)
                ivCircle2.setImageResource(R.drawable.shape_circle_selected)
                ivCircle3.setImageResource(R.drawable.shape_circle_not_selected)
            }

            2 -> {
                ivCircle1.setImageResource(R.drawable.shape_circle_not_selected)
                ivCircle2.setImageResource(R.drawable.shape_circle_not_selected)
                ivCircle3.setImageResource(R.drawable.shape_circle_selected)
            }
        }
    }
}
