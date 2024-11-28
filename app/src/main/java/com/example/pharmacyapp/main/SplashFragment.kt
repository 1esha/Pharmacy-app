package com.example.pharmacyapp.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.pharmacyapp.R
import com.example.pharmacyapp.databinding.FragmentSplashBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val navControllerMain = findNavController()

        //isInit отвечает за то надо ли показывать пользователю InitFragment
        var isInit = true

            CoroutineScope(Dispatchers.Main).launch {
                delay(DEFAULT_DELAY)
                if (isInit){
                    navControllerMain.navigate(R.id.action_splashFragment_to_initFragment,null, navOptions {
                        popUpTo(R.id.splashFragment){
                            inclusive = true
                        }
                    })
                }
                else{
                    navControllerMain.navigate(R.id.action_splashFragment_to_tabsFragment,null, navOptions {
                        popUpTo(R.id.splashFragment){
                            inclusive = true
                        }
                    })
                }
            }



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val DEFAULT_DELAY = 1500L
    }

}