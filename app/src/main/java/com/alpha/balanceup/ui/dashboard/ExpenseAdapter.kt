package com.alpha.balanceup.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.databinding.ItemExpenseBinding
import com.google.android.material.card.MaterialCardView

class ExpenseAdapter(
    private val onExpenseClick: (ExpenseItemEntity) -> Unit,
    private val onExpenseLongClick: (ExpenseItemEntity) -> Unit,
    private val onSelectionChange: (Int) -> Unit
) : ListAdapter<ExpenseItemEntity, ExpenseAdapter.ViewHolder>(DiffCallback()) {

    private val selectedItems = mutableSetOf<Long>()
    private var isSelectionMode = false

    fun setSelectionMode(enabled: Boolean) {
        if (isSelectionMode == enabled) return
        isSelectionMode = enabled
        if (!enabled) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    fun isSelectionMode() = isSelectionMode

    fun toggleSelection(expenseId: Long) {
        if (selectedItems.contains(expenseId)) {
            selectedItems.remove(expenseId)
        } else {
            selectedItems.add(expenseId)
        }
        notifyDataSetChanged()
        onSelectionChange(selectedItems.size)
    }

    fun getSelectedIds(): List<Long> = selectedItems.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, selectedItems.contains(item.id))
        
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(item.id)
            } else {
                onExpenseClick(item)
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                onExpenseLongClick(item)
                toggleSelection(item.id)
                true
            } else {
                false
            }
        }
    }

    class ViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseItemEntity, isSelected: Boolean) {
            binding.tvProductName.text = item.productName
            // Display only the alias part if it's a guest-style name
            val displayName = if (item.paidBy.contains("_")) item.paidBy.substringBeforeLast("_") else item.paidBy
            binding.tvPaidBy.text = "Paid by: $displayName"
            binding.tvPrice.text = "₹ ${String.format("%.2f", item.price)}"
            
            val cardView = binding.root as MaterialCardView
            cardView.isChecked = isSelected
            cardView.strokeWidth = if (isSelected) 4 else 0
            cardView.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#6200EE")))
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
