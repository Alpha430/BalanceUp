package com.alpha.balanceup.data.repository

import com.alpha.balanceup.data.local.dao.ExpenseDao
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun createGroup(name: String): Long {
        return expenseDao.insertGroup(GroupEntity(name = name))
    }

    suspend fun addExpenseItem(item: ExpenseItemEntity) {
        expenseDao.insertExpenseItem(item)
    }

    fun getExpensesForGroup(groupId: Long): Flow<List<ExpenseItemEntity>> {
        return expenseDao.getExpensesForGroup(groupId)
    }

    fun getTotalExpenseForGroup(groupId: Long): Flow<Double?> {
        return expenseDao.getTotalExpenseForGroup(groupId)
    }
}
