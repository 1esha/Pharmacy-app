package com.example.pharmacyapp.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.models.CatalogMainModel
import com.example.pharmacyapp.KEY_ARRAY_LIST_CURRENT_ITEMS
import com.example.pharmacyapp.KEY_PATH_MAIN
import com.example.pharmacyapp.R
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentCatalogBinding
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.CatalogMainAdapter
import com.example.pharmacyapp.toPath

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private lateinit var navControllerCatalog: NavController

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){

        navControllerCatalog = findNavController()

        toolbarViewModel.setToolbarSettings(toolbarSettingsModel = ToolbarSettingsModel(title = getString(R.string.catalog)){})
        toolbarViewModel.setMenuSettings()

        val listItems = listOf(
            CatalogMainModel(image = R.drawable.medicinal_products, title = getString(R.string.medicines_and_dietary_supplements)),
            CatalogMainModel(image = R.drawable.medical_devices, title = getString(R.string.medical_devices)),
            CatalogMainModel(image = R.drawable.hygiene_and_care, title = getString(R.string.hygiene_and_care)),
            CatalogMainModel(image = R.drawable.optics, title = getString(R.string.optics)),
            CatalogMainModel(image = R.drawable.mother_and_baby, title = getString(R.string.mom_and_baby)),
            CatalogMainModel(image = R.drawable.orthopedic_products, title = getString(R.string.orthopedic_products))
        )

        val catalogMainAdapter = CatalogMainAdapter(listItems = listItems) { path ->
            onClickCatalogMain(path = path)
        }

        rvCatalog.adapter = catalogMainAdapter
        rvCatalog.layoutManager = GridLayoutManager(requireContext(), 2)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickCatalogMain(path: String) {

        val arrayListMedicinesAndDietarySupplements = arrayListOf(
            getString(R.string.blood_pressure_relief_products),
            getString(R.string.vitamins_for_the_heart_blood_vessels),
            getString(R.string.blood_pressure_medications),
            getString(R.string.restoration_of_the_gastrointestinal_microflora),
            getString(R.string.anti_ulcer_agents),
            getString(R.string.heartburn),
            getString(R.string.antimicrobial_agents),
            getString(R.string.antiviral_agents),
            getString(R.string.remedies_for_sore_throat),
            getString(R.string.remedies_for_the_common_cold),
            getString(R.string.cough_remedies),
            getString(R.string.sedatives),
            getString(R.string.sleeping_pills),
            getString(R.string.vitamins_for_the_whole_family),
            getString(R.string.painkillers),
            getString(R.string.antibiotics),
            getString(R.string.herbs)
        )

        val arrayListMedicalDevices = arrayListOf(
            getString(R.string.toothpaste),
            getString(R.string.mouthwash),
            getString(R.string.toothbrushes_floss),
            getString(R.string.blood_pressure_monitors),
            getString(R.string.inhalers),
            getString(R.string.thermometers),
            getString(R.string.glucose_meters),
            getString(R.string.applicators),
            getString(R.string.massagers),
            getString(R.string.crutches_canes)
        )

        val arrayListHygieneAndCare = arrayListOf(
            getString(R.string.ear_hygiene),
            getString(R.string.acne_treatment),
            getString(R.string.diapers),
            getString(R.string.cotton_pads),
            getString(R.string.soap),
            getString(R.string.antiseptic_gels_and_sprays),
            getString(R.string.shaving_products),
            getString(R.string.cotton_swabs),
            getString(R.string.toilet_paper),
            getString(R.string.napkins),
            getString(R.string.washcloths),
            getString(R.string.shoe_covers)
            )

        val arrayListOptics = arrayListOf(
            getString(R.string.contact_lens_solutions),
            getString(R.string.contact_lenses),
            getString(R.string.glasses_for_vision),
            getString(R.string.accessories_for_glasses_and_lenses)
        )

        val arrayListMomAndBaby = arrayListOf(
            getString(R.string.baby_soap),
            getString(R.string.diapers_for_children),
            getString(R.string.baby_toothbrushes),
            getString(R.string.pacifier_nipples),
            getString(R.string.children_s_accessories),
            getString(R.string.feeding_bottles_drinking_bowls),
            getString(R.string.bottle_pacifier),
            getString(R.string.children_s_tableware),
            getString(R.string.pacifier_clip),
            getString(R.string.bibs_and_bibs),
            getString(R.string.rattles),
            getString(R.string.bottle_brushes),
            getString(R.string.powder)
        )

        val arrayListOrthopedicProducts = arrayListOf(
            getString(R.string.orthopedic_belt),
            getString(R.string.bandages),
            getString(R.string.the_reclinator),
            getString(R.string.the_mattress_is_decongestant),
            getString(R.string.orthopedic_corset_retainer),
            getString(R.string.orthopedic_insoles),
            getString(R.string.orthopedic_pillows)
        )

        val medicinesAndDietarySupplements = getString(R.string.medicines_and_dietary_supplements).toPath()
        val medicalDevices = getString(R.string.medical_devices).toPath()
        val hygieneAndCare = getString(R.string.hygiene_and_care).toPath()
        val optics = getString(R.string.optics).toPath()
        val momAndBaby = getString(R.string.mom_and_baby).toPath()
        val orthopedicProducts = getString(R.string.orthopedic_products).toPath()

        val arrayListCurrentItems = when(path){
            medicinesAndDietarySupplements -> arrayListMedicinesAndDietarySupplements
            medicalDevices -> arrayListMedicalDevices
            hygieneAndCare -> arrayListHygieneAndCare
            optics -> arrayListOptics
            momAndBaby -> arrayListMomAndBaby
            orthopedicProducts -> arrayListOrthopedicProducts
            else -> arrayListOf()
        }

        val bundle = Bundle()
        bundle.putString(KEY_PATH_MAIN,path)
        bundle.putStringArrayList(KEY_ARRAY_LIST_CURRENT_ITEMS, arrayListCurrentItems)
        navControllerCatalog.navigate(R.id.action_catalogFragment_to_subdirectoryFragment, bundle)
    }

}