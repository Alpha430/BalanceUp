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
    private val _expenseId = MutableStateFlow<Long?>(null)
    
    private val _existingExpense = MutableStateFlow<ExpenseItemEntity?>(null)
    val existingExpense = _existingExpense.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    val groupMembers: Flow<List<GroupMemberEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getMembersForGroup(id) else flowOf(emptyList())
    }

    fun setGroupId(id: Long) {
        _groupId.value = id
    }

    fun setExpenseId(id: Long) {
        _expenseId.value = id
        viewModelScope.launch {
            val expense = repository.getExpenseById(id)
            _existingExpense.value = expense
            if (expense != null) {
                _groupId.value = expense.groupId
            }
        }
    }

    fun saveExpense(productName: String, price: String, paidBy: String) {
        val gId = _groupId.value ?: return
        val prc = price.toDoubleOrNull() ?: 0.0
        val expId = _expenseId.value ?: 0L

        viewModelScope.launch {
            val item = if (expId != 0L) {
                _existingExpense.value?.copy(
                    productName = productName,
                    price = prc,
                    paidBy = paidBy
                ) ?: ExpenseItemEntity(
                    id = expId,
                    groupId = gId,
                    productName = productName,
                    price = prc,
                    paidBy = paidBy
                )
            } else {
                ExpenseItemEntity(
                    groupId = gId,
                    productName = productName,
                    price = prc,
                    paidBy = paidBy
                )
            }

            if (expId != 0L) {
                repository.updateExpenseItem(item)
            } else {
                repository.addExpenseItem(item)
            }
            _saveSuccess.emit(true)
        }
    }
}
