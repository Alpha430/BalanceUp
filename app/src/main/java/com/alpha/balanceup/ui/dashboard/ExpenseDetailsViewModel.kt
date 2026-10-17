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
import kotlin.math.abs
import kotlin.math.min

@HiltViewModel
class ExpenseDetailsViewModel @Inject constructor(
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

    val groupMembers: Flow<List<GroupMemberEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getMembersForGroup(id) else flowOf(emptyList())
    }

    val totalExpense: Flow<Double> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getTotalExpenseForGroup(id).map { it ?: 0.0 } else flowOf(0.0)
    }

    fun deleteGroup() {
        val id = _groupId.value ?: return
        viewModelScope.launch {
            repository.deleteGroup(id)
        }
    }

    fun deleteExpenses(expenseIds: List<Long>) {
        viewModelScope.launch {
            repository.deleteExpenseItems(expenseIds)
        }
    }

    fun markAsCalculated() {
        val id = _groupId.value ?: return
        viewModelScope.launch {
            repository.updateGroupCalculationStatus(id, true)
        }
    }

    fun getSettlementSummary(members: List<GroupMemberEntity>, expenses: List<ExpenseItemEntity>): SettlementSummary {
        if (members.isEmpty()) return SettlementSummary(0.0, 0.0, emptyList(), emptyList())

        val totalPaidByMember = mutableMapOf<String, Double>()
        members.forEach { totalPaidByMember[it.name] = 0.0 }
        
        expenses.forEach { expense ->
            val current = totalPaidByMember[expense.paidBy] ?: 0.0
            totalPaidByMember[expense.paidBy] = current + expense.price
        }

        val totalGroupExpense = expenses.sumOf { it.price }
        val sharePerPerson = if (members.isNotEmpty()) totalGroupExpense / members.size else 0.0

        val balances = members.map { member ->
            val paid = totalPaidByMember[member.name] ?: 0.0
            val net = paid - sharePerPerson
            MemberBalance(member.name, paid, net)
        }

        val settlements = mutableListOf<Settlement>()
        
        val debtors = balances.filter { it.net < -0.01 }.sortedBy { it.net }.toMutableList()
        val creditors = balances.filter { it.net > 0.01 }.sortedByDescending { it.net }.toMutableList()

        var dIdx = 0
        var cIdx = 0

        val tempDebtors = debtors.map { it.name to it.net }.toMutableList()
        val tempCreditors = creditors.map { it.name to it.net }.toMutableList()

        while (dIdx < tempDebtors.size && cIdx < tempCreditors.size) {
            val debtor = tempDebtors[dIdx]
            val creditor = tempCreditors[cIdx]

            val amount = min(abs(debtor.second), creditor.second)
            if (amount > 0.01) {
                settlements.add(Settlement(debtor.first, creditor.first, amount))
            }

            tempDebtors[dIdx] = debtor.first to (debtor.second + amount)
            tempCreditors[cIdx] = creditor.first to (creditor.second - amount)

            if (abs(tempDebtors[dIdx].second) < 0.01) dIdx++
            if (abs(tempCreditors[cIdx].second) < 0.01) cIdx++
        }

        return SettlementSummary(totalGroupExpense, sharePerPerson, balances, settlements)
    }

    data class MemberBalance(val name: String, val totalPaid: Double, val net: Double)
    data class Settlement(val from: String, val to: String, val amount: Double)
    data class SettlementSummary(
        val totalGroupExpense: Double,
        val sharePerPerson: Double,
        val memberBalances: List<MemberBalance>,
        val settlements: List<Settlement>
    )
}
