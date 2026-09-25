package com.alpha.balanceup.data.local.dao

import androidx.room.*
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItem(item: ExpenseItemEntity)

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): GroupEntity?

    @Query("SELECT * FROM expense_items WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getExpensesForGroup(groupId: Long): Flow<List<ExpenseItemEntity>>

    @Query("SELECT SUM(price * quantity) FROM expense_items WHERE groupId = :groupId")
    fun getTotalExpenseForGroup(groupId: Long): Flow<Double?>
}
