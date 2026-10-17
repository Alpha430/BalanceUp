package com.alpha.balanceup.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.R
import com.alpha.balanceup.databinding.ItemMemberBalanceBinding

class MemberBalanceAdapter : ListAdapter<ExpenseDetailsViewModel.MemberBalance, MemberBalanceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemberBalanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemMemberBalanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseDetailsViewModel.MemberBalance) {
            val context = binding.root.context
            // Display only the alias part if it's a guest-style name
            binding.tvMemberName.text = if (item.name.contains("_")) item.name.substringBeforeLast("_") else item.name
            binding.tvTotalPaid.text = "Paid: ₹ ${String.format("%.2f", item.totalPaid)}"
            
            val netValue = Math.abs(item.net)
            if (item.net >= 0.01) {
                binding.tvNetStatus.text = "gets back ₹ ${String.format("%.2f", netValue)}"
                binding.tvNetStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.positive_balance
                    )
                )
            } else if (item.net <= -0.01) {
                binding.tvNetStatus.text = "owes ₹ ${String.format("%.2f", netValue)}"
                binding.tvNetStatus.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.negative_balance
                    )
                )
            } else {
                binding.tvNetStatus.text = "settled up"
                binding.tvNetStatus.setTextColor(ContextCompat.getColor(context, R.color.text_hint))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpenseDetailsViewModel.MemberBalance>() {
        override fun areItemsTheSame(oldItem: ExpenseDetailsViewModel.MemberBalance, newItem: ExpenseDetailsViewModel.MemberBalance): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: ExpenseDetailsViewModel.MemberBalance, newItem: ExpenseDetailsViewModel.MemberBalance): Boolean {
            return oldItem == newItem
        }
    }
}
