package com.alpha.balanceup.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.data.local.entity.GroupWithMemberCount
import com.alpha.balanceup.databinding.ItemGroupBinding

class GroupAdapter(private val onGroupClick: (GroupWithMemberCount) -> Unit) : 
    ListAdapter<GroupWithMemberCount, GroupAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onGroupClick(item) }
    }

    class ViewHolder(private val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GroupWithMemberCount) {
            binding.tvGroupName.text = item.group.name
            binding.tvMemberCount.text = "${item.memberCount} members"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GroupWithMemberCount>() {
        override fun areItemsTheSame(oldItem: GroupWithMemberCount, newItem: GroupWithMemberCount): Boolean {
            return oldItem.group.id == newItem.group.id
        }

        override fun areContentsTheSame(oldItem: GroupWithMemberCount, newItem: GroupWithMemberCount): Boolean {
            return oldItem == newItem
        }
    }
}
