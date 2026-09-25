package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _groupId = MutableStateFlow<Long?>(null)
    val groupId: StateFlow<Long?> = _groupId.asStateFlow()

    val expenseItems: Flow<List<ExpenseItemEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getExpensesForGroup(id) else flowOf(emptyList())
    }

    val totalExpense: Flow<Double> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getTotalExpenseForGroup(id).map { it ?: 0.0 } else flowOf(0.0)
    }

    fun createGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createGroup(name)
            _groupId.value = id
        }
    }

    fun addExpenseItem(productName: String, quantity: String, price: String, paidBy: String) {
        val gId = _groupId.value ?: return
        val qty = quantity.toIntOrNull() ?: 1
        val prc = price.toDoubleOrNull() ?: 0.0

        if (productName.isBlank() || paidBy.isBlank()) return

        viewModelScope.launch {
            val item = ExpenseItemEntity(
                groupId = gId,
                productName = productName,
                quantity = qty,
                price = prc,
                paidBy = paidBy
            )
            repository.addExpenseItem(item)
        }
    }
}
