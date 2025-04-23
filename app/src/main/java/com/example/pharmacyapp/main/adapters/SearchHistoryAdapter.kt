package com.example.pharmacyapp.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.domain.search.models.SearchQueryModel
import com.example.pharmacyapp.databinding.ClearSearchHistoryBinding
import com.example.pharmacyapp.databinding.ItemSearchHistoryBinding

class SearchHistoryAdapter(
    private val mutableListSearchQueryModel: MutableList<SearchQueryModel>,
    private val onClickSearchQuery: (SearchQueryModel) -> Unit,
    private val deleteSearchQuery: (Int) -> Unit,
    private val clearAllHistory: () -> Unit
): Adapter<ViewHolder>() {

    class SearchHistoryHolder(val binding: ItemSearchHistoryBinding): ViewHolder(binding.root)

    class ClearSearchHistoryHolder(val binding: ClearSearchHistoryBinding): ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val bindingSearchHistory = ItemSearchHistoryBinding.inflate(inflater,parent,false)
        val bindingClearSearchHistory = ClearSearchHistoryBinding.inflate(inflater,parent,false)

        return if (viewType > 0) ClearSearchHistoryHolder(bindingClearSearchHistory) else SearchHistoryHolder(bindingSearchHistory)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == mutableListSearchQueryModel.size) 1 else 0
    }

    override fun getItemCount(): Int = mutableListSearchQueryModel.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder){
            is SearchHistoryHolder -> {
                with(holder.binding) {
                    val item = mutableListSearchQueryModel[position]

                    tvSearchTextHistory.text = item.searchText

                    tvSearchTextHistory.setOnClickListener {
                        onClickSearchQuery(item)
                    }

                    ivDeleteSearchQuery.setOnClickListener {
                        deleteSearchQuery(item.searchQueryId)
                    }

                }
            }
            is ClearSearchHistoryHolder -> {
                with(holder.binding) {
                    bClearSearchHistory.setOnClickListener {
                        clearAllHistory()
                    }
                }
            }
        }
    }
}