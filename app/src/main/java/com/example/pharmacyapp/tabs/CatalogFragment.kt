package com.example.pharmacyapp.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.models.CatalogMainModel
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentCatalogBinding
import com.example.pharmacyapp.tabs.catalog.CatalogMainAdapter

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){

        val listItems = listOf(
            CatalogMainModel(image = R.drawable.medicinal_products, title = getString(R.string.medicines_and_dietary_supplements)),
            CatalogMainModel(image = R.drawable.medical_devices, title = getString(R.string.medical_devices)),
            CatalogMainModel(image = R.drawable.hygiene_and_care, title = getString(R.string.hygiene_and_care)),
            CatalogMainModel(image = R.drawable.optics, title = getString(R.string.optics)),
            CatalogMainModel(image = R.drawable.mother_and_baby, title = getString(R.string.mom_and_baby)),
            CatalogMainModel(image = R.drawable.orthopedic_products, title = getString(R.string.orthopedic_products))
        )

        val catalogMainAdapter = CatalogMainAdapter(listItems = listItems) { path ->

        }

        rvCatalog.adapter = catalogMainAdapter
        rvCatalog.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}