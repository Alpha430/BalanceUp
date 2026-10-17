package com.alpha.balanceup.ui.dashboard

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.databinding.ItemManualShareBinding

class ManualShareAdapter : RecyclerView.Adapter<ManualShareAdapter.ViewHolder>() {

    private var membersList: List<GroupMemberEntity> = emptyList()
    private val sharesMap = mutableMapOf<Long, Double>()
    private var isEditable: Boolean = true

    fun submitList(list: List<GroupMemberEntity>) {
        this.membersList = list
        notifyDataSetChanged()
    }

    fun setShares(shares: Map<Long, Double>) {
        sharesMap.clear()
        sharesMap.putAll(shares)
        notifyDataSetChanged()
    }

    fun setEditable(editable: Boolean) {
        this.isEditable = editable
        notifyDataSetChanged()
    }

    fun getManualShares(): Map<Long, Double> {
        return sharesMap
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemManualShareBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(membersList[position])
    }

    override fun getItemCount(): Int = membersList.size

    inner class ViewHolder(private val binding: ItemManualShareBinding) : RecyclerView.ViewHolder(binding.root) {
        private var currentWatcher: TextWatcher? = null

        fun bind(member: GroupMemberEntity) {
            // Display only the alias part if it's a guest-style name
            binding.tvMemberName.text = if (member.name.contains("_")) member.name.substringBeforeLast("_") else member.name
            
            // Cleanup to avoid infinite loops and recycling issues
            currentWatcher?.let { binding.etAmount.removeTextChangedListener(it) }

            val amount = sharesMap[member.id] ?: 0.0
            
            // Format to 2 decimal places. 
            // In manual mode, we show empty string for 0 to make it easier to type.
            if (!isEditable) {
                binding.etAmount.setText(String.format("%.2f", amount))
            } else {
                binding.etAmount.setText(if (amount > 0) String.format("%.2f", amount) else "")
            }

            binding.etAmount.isEnabled = isEditable

            currentWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (!isEditable) return
                    val amountStr = s.toString().trim()
                    if (amountStr.isNotEmpty()) {
                        sharesMap[member.id] = amountStr.toDoubleOrNull() ?: 0.0
                    } else {
                        sharesMap.remove(member.id)
                    }
                }
            }
            binding.etAmount.addTextChangedListener(currentWatcher)
        }
    }
}
