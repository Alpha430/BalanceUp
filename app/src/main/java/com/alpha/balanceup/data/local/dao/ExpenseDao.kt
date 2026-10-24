package com.alpha.balanceup.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.local.entity.GroupEntity
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.data.local.entity.GroupWithMemberCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity)

    @Delete
    suspend fun deleteGroupMember(member: GroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItem(item: ExpenseItemEntity)

    @Update
    suspend fun updateExpenseItem(item: ExpenseItemEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)

    @Query("UPDATE groups SET isCalculated = :isCalculated WHERE id = :groupId")
    suspend fun updateGroupCalculationStatus(groupId: Long, isCalculated: Boolean)

    @Query("DELETE FROM expense_items WHERE id IN (:expenseIds)")
    suspend fun deleteExpenseItems(expenseIds: List<Long>)

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Transaction
    @Query("""
        SELECT groups.*, (SELECT COUNT(*) FROM group_members WHERE groupId = groups.id) as memberCount 
        FROM groups 
        ORDER BY createdAt DESC
    """)
    fun getAllGroupsWithMemberCount(): Flow<List<GroupWithMemberCount>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Long): GroupEntity?

    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun getGroupByIdFlow(groupId: Long): Flow<GroupEntity?>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY id DESC")
    fun getMembersForGroup(groupId: Long): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM expense_items WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getExpensesForGroup(groupId: Long): Flow<List<ExpenseItemEntity>>

    @Query("SELECT * FROM expense_items WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Long): ExpenseItemEntity?

    @Query("SELECT SUM(price) FROM expense_items WHERE groupId = :groupId")
    fun getTotalExpenseForGroup(groupId: Long): Flow<Double?>

    @Query("UPDATE group_members SET name = :newName WHERE name = :oldName")
    suspend fun updateMemberName(oldName: String, newName: String)

    @Query("UPDATE expense_items SET paidBy = :newName WHERE paidBy = :oldName")
    suspend fun updateExpensePaidBy(oldName: String, newName: String)
}
