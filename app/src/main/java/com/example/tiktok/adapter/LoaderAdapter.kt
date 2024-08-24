package com.example.tiktok.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tiktok.databinding.ItemLoaderBinding

class LoaderAdapter : LoadStateAdapter<LoaderAdapter.LoaderViewHolder>() {

    override fun onBindViewHolder(holder: LoaderViewHolder, loadState: LoadState) {
        holder.binding(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLoaderBinding.inflate(inflater, parent, false)
        return LoaderViewHolder(binding)
    }

    inner class LoaderViewHolder(private val itemBinding: ItemLoaderBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun binding(loadState: LoadState) {
            itemBinding.progressBar.isVisible =
                loadState is LoadState.Loading || loadState is LoadState.Error
        }
    }
}