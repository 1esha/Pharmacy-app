package com.example.pharmacyapp.tabs.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettings
import com.example.pharmacyapp.databinding.FragmentFullImageProductBinding

/**
 * Класс FullImageProductFragment отвечает за отображение полной картинки товара.
 * Фргамет находится в главном графе навигации (nav_graph_main.xml) и
 * открывается поверх вкладок (экарн TabsFragment) при нажатии на товар на экране с информацией товара.
 */

class FullImageProductFragment: Fragment() {

    private var _binding: FragmentFullImageProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullImageProductBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        val image = arguments?.getString(KEY_FULL_IMAGE_PRODUCT) ?:
        throw NullPointerException("FullImageProductFragment image = null")

        val navControllerMain = findNavController()

        // установка toolbar
        val toolbarSettings = ToolbarSettings(toolbar = layoutToolbarMainFillImage.toolbarMain)
        toolbarSettings.installToolbarMain(icon = R.drawable.ic_close) {
            navControllerMain.navigateUp()
        }

        // загрузка изобажения товара
        ivFullImageProduct.load(image)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_FULL_IMAGE_PRODUCT = "KEY_FULL_IMAGE_PRODUCT"
    }

}