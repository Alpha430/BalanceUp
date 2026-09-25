package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class GroupDetailsViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _groupId = MutableStateFlow<Long?>(null)
    val groupId: StateFlow<Long?> = _groupId.asStateFlow()

    fun setGroupId(id: Long) {
        _groupId.value = id
    }

    val expenseItems: Flow<List<ExpenseItemEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getExpensesForGroup(id) else flowOf(emptyList())
    }

    val totalExpense: Flow<Double> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getTotalExpenseForGroup(id).map { it ?: 0.0 } else flowOf(0.0)
    }
}
