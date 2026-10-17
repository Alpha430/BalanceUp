package com.alpha.balanceup.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.databinding.ItemSettlementBinding

class SettlementAdapter : ListAdapter<ExpenseDetailsViewModel.Settlement, SettlementAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettlementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemSettlementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseDetailsViewModel.Settlement) {
            // Display only the alias part if it's a guest-style name
            binding.tvFromMember.text = if (item.from.contains("_")) item.from.substringBeforeLast("_") else item.from
            binding.tvToMember.text = if (item.to.contains("_")) item.to.substringBeforeLast("_") else item.to
            binding.tvAmount.text = "₹ ${String.format("%.2f", item.amount)}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpenseDetailsViewModel.Settlement>() {
        override fun areItemsTheSame(oldItem: ExpenseDetailsViewModel.Settlement, newItem: ExpenseDetailsViewModel.Settlement): Boolean {
            return oldItem.from == newItem.from && oldItem.to == newItem.to
        }

        override fun areContentsTheSame(oldItem: ExpenseDetailsViewModel.Settlement, newItem: ExpenseDetailsViewModel.Settlement): Boolean {
            return oldItem == newItem
        }
    }
}
