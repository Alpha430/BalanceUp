package com.alpha.balanceup.data.local.dao

import androidx.room.*
import com.alpha.balanceup.data.local.entity.ExpenseItemEntity
import com.alpha.balanceup.data.local.entity.GroupEntity
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.data.local.entity.GroupWithMemberCount
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItem(item: ExpenseItemEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)

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

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getMembersForGroup(groupId: Long): Flow<List<GroupMemberEntity>>

    @Query("SELECT * FROM expense_items WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getExpensesForGroup(groupId: Long): Flow<List<ExpenseItemEntity>>

    @Query("SELECT SUM(price) FROM expense_items WHERE groupId = :groupId")
    fun getTotalExpenseForGroup(groupId: Long): Flow<Double?>
}
