package com.example.pharmacyapp.main.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.domain.models.PageModel
import com.example.pharmacyapp.databinding.ItemInitBinding

class InitAdapter(
    private val listPages: List<PageModel>,
    private val onClickSignIn:() -> Unit,
    private val onClickSignUp:() -> Unit
    ) : RecyclerView.Adapter<InitAdapter.InitHolder>() {


    class InitHolder(val binding: ItemInitBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InitHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInitBinding.inflate(inflater, parent, false)
        return InitHolder(binding)
    }

    override fun getItemCount(): Int = listPages.size

    override fun onBindViewHolder(holder: InitHolder, position: Int) = with(holder.binding) {
        layoutLoginButtons.visibility = if (position == listPages.size-1) View.VISIBLE else View.INVISIBLE

        val page = listPages[position]
        tvDescription.text = page.description

        bSignIn.setOnClickListener { onClickSignIn() }
        bSignUp.setOnClickListener { onClickSignUp() }
    }
}