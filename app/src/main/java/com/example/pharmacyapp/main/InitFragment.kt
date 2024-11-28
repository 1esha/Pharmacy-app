package com.example.pharmacyapp.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.viewpager2.widget.ViewPager2
import com.example.domain.models.PageModel
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentInitBinding
import com.example.pharmacyapp.main.adapters.InitAdapter

class InitFragment : Fragment() {

    private var _binding: FragmentInitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInitBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        val listPages = listOf(
            PageModel("Бронируйте товары в аптеках по выгодной цене", 0),
            PageModel("Получайте скидки", 0),
            PageModel("Войдите, чтобы получать больше выгоды!", 0)
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
        navControllerMain.navigate(R.id.action_initFragment_to_loginFragment)

    }

    private fun onClickSkip(){
        val navControllerMain = findNavController()
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
