package com.dicoding.mommymunch.ui.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.mommymunch.data.response.ResponseHomeFoodItem
import com.dicoding.mommymunch.databinding.HomeItemRowBinding

class HomeAdapter(private val listUsers: List<ResponseHomeFoodItem>) :
    ListAdapter<ResponseHomeFoodItem, HomeAdapter.MyViewHolder>(DIFF_CALLBACK) {

    private lateinit var onItemClickCallback: OnItemClickCallback

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = HomeItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val items = getItem(position)
        holder.bind(items)
        holder.itemView.setOnClickListener {
            onItemClickCallback.onItemClicked(listUsers[holder.adapterPosition])
        }
    }

    class MyViewHolder(val binding: HomeItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(items: ResponseHomeFoodItem) {
            Glide.with(itemView.context)
                .load(items.image)
                .into(binding.ivItem)
            binding.tvItemTitle.text = items.name
            binding.tvItemContent.text = "Proteins: ${items.proteins}, Fat: ${items.fat}, Calories: ${items.calories}, Carbohydrate: ${items.carbohydrate}"
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ResponseHomeFoodItem>() {
            override fun areItemsTheSame(oldItem: ResponseHomeFoodItem, newItem: ResponseHomeFoodItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ResponseHomeFoodItem, newItem: ResponseHomeFoodItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ResponseHomeFoodItem)
    }

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }
}