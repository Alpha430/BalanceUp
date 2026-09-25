package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseItemViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _groupId = MutableStateFlow<Long?>(null)
    
    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    val groupMembers: Flow<List<GroupMemberEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getMembersForGroup(id) else flowOf(emptyList())
    }

    fun setGroupId(id: Long) {
        _groupId.value = id
    }

    fun saveExpense(productName: String, quantity: String, price: String, paidBy: String) {
        val gId = _groupId.value ?: return
        val qty = quantity.toIntOrNull() ?: 1
        val prc = price.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            val item = ExpenseItemEntity(
                groupId = gId,
                productName = productName,
                quantity = qty,
                price = prc,
                paidBy = paidBy
            )
            repository.addExpenseItem(item)
            _saveSuccess.emit(true)
        }
    }
}
