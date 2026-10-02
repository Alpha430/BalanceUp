package com.alpha.balanceup.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.databinding.ItemExpenseBinding

class ExpenseAdapter(private val onExpenseClick: (ExpenseItemEntity) -> Unit) : 
    ListAdapter<ExpenseItemEntity, ExpenseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onExpenseClick(item) }
    }

    class ViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseItemEntity) {
            binding.tvProductName.text = item.productName
            binding.tvPaidBy.text = "Paid by: ${item.paidBy}"
            binding.tvPrice.text = "₹ ${String.format("%.2f", item.price)}"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpenseItemEntity>() {
        override fun areItemsTheSame(oldItem: ExpenseItemEntity, newItem: ExpenseItemEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExpenseItemEntity, newItem: ExpenseItemEntity): Boolean {
            return oldItem == newItem
        }
    }
}
