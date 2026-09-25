package com.alpha.balanceup.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.databinding.ItemMemberBinding

class MemberAdapter : ListAdapter<GroupMemberEntity, MemberAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupMemberEntity) {
            binding.tvMemberName.text = item.name
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
