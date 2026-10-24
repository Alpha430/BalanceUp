package com.alpha.balanceup.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.databinding.ItemMemberBinding

class MemberAdapter(
    private val onDeleteClick: (GroupMemberEntity) -> Unit
) : ListAdapter<GroupMemberEntity, MemberAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemMemberBinding,
        private val onDeleteClick: (GroupMemberEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupMemberEntity) {
            // Display only the alias part (before the underscore) if it's a guest-style name
            binding.tvMemberName.text = if (item.name.contains("_")) item.name.substringBeforeLast("_") else item.name

            binding.ivDelete.setOnClickListener {
                onDeleteClick(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GroupMemberEntity>() {
        override fun areItemsTheSame(oldItem: GroupMemberEntity, newItem: GroupMemberEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GroupMemberEntity, newItem: GroupMemberEntity): Boolean {
            return oldItem == newItem
        }
    }
}
