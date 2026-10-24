package com.alpha.balanceup.data.repository

import com.alpha.balanceup.data.local.dao.ExpenseDao
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.local.entity.GroupEntity
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.data.local.entity.GroupWithMemberCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    fun getAllGroups(): Flow<List<GroupEntity>> {
        return expenseDao.getAllGroups()
    }

    fun getAllGroupsWithMemberCount(): Flow<List<GroupWithMemberCount>> {
        return expenseDao.getAllGroupsWithMemberCount()
    }

    suspend fun createGroup(name: String): Long {
        return expenseDao.insertGroup(GroupEntity(name = name))
    }

    suspend fun addMember(member: GroupMemberEntity) {
        expenseDao.insertGroupMember(member)
    }

    suspend fun deleteMember(member: GroupMemberEntity) {
        expenseDao.deleteGroupMember(member)
    }

    fun getMembersForGroup(groupId: Long): Flow<List<GroupMemberEntity>> {
        return expenseDao.getMembersForGroup(groupId)
    }

    suspend fun addExpenseItem(item: ExpenseItemEntity) {
        expenseDao.insertExpenseItem(item)
        // Mark group as not calculated when new expense is added
        updateGroupCalculationStatus(item.groupId, false)
    }

    suspend fun updateExpenseItem(item: ExpenseItemEntity) {
        expenseDao.updateExpenseItem(item)
        // Mark group as not calculated when expense is updated
        updateGroupCalculationStatus(item.groupId, false)
    }

    suspend fun getExpenseById(expenseId: Long): ExpenseItemEntity? {
        return expenseDao.getExpenseById(expenseId)
    }

    suspend fun deleteGroup(groupId: Long) {
        expenseDao.deleteGroupById(groupId)
    }

    suspend fun deleteExpenseItems(expenseIds: List<Long>) {
        if (expenseIds.isNotEmpty()) {
            val firstExpense = getExpenseById(expenseIds[0])
            expenseDao.deleteExpenseItems(expenseIds)
            firstExpense?.let { updateGroupCalculationStatus(it.groupId, false) }
        }
    }

    fun getExpensesForGroup(groupId: Long): Flow<List<ExpenseItemEntity>> {
        return expenseDao.getExpensesForGroup(groupId)
    }

    fun getTotalExpenseForGroup(groupId: Long): Flow<Double?> {
        return expenseDao.getTotalExpenseForGroup(groupId)
    }

    suspend fun updateGroupCalculationStatus(groupId: Long, isCalculated: Boolean) {
        expenseDao.updateGroupCalculationStatus(groupId, isCalculated)
    }
}
