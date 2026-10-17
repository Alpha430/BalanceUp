package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.balanceup.data.local.entity.GroupWithMemberCount
import com.alpha.balanceup.data.repository.ExpenseRepository
import com.alpha.balanceup.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val allGroups: Flow<List<GroupWithMemberCount>> = repository.getAllGroupsWithMemberCount()

    val userAlias: Flow<String> = userRepository.getUserFlow().map { user ->
        user?.name?.substringBeforeLast("_") ?: "Guest"
    }

    private val userProfileName: Flow<String?> = userRepository.getUserFlow().map { it?.name }

    val totalSpends: Flow<Double> = combine(userProfileName, repository.getAllGroups()) { profileName, groups ->
        if (profileName == null) return@combine 0.0
        var total = 0.0
        for (group in groups) {
            val expenses = repository.getExpensesForGroup(group.id).first()
            total += expenses.filter { it.paidBy == profileName }.sumOf { it.price }
        }
        total
    }

    val youGet: Flow<Double> = combine(userProfileName, repository.getAllGroups()) { profileName, groups ->
        if (profileName == null) return@combine 0.0
        var totalOwed = 0.0
        for (group in groups) {
            val members = repository.getMembersForGroup(group.id).first()
            val expenses = repository.getExpensesForGroup(group.id).first()
            
            val totalGroupExpense = expenses.sumOf { it.price }
            val sharePerPerson = if (members.isNotEmpty()) totalGroupExpense / members.size else 0.0
            
            val userPaid = expenses.filter { it.paidBy == profileName }.sumOf { it.price }
            val net = userPaid - sharePerPerson
            
            if (net > 0) {
                totalOwed += net
            }
        }
        totalOwed
    }
}
