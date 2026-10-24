package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.data.repository.ExpenseRepository
import com.alpha.balanceup.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _groupId = MutableStateFlow<Long?>(null)
    val groupId: StateFlow<Long?> = _groupId.asStateFlow()

    val groupMembers: Flow<List<GroupMemberEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getMembersForGroup(id) else flowOf(emptyList())
    }

    fun createGroup(name: String, includeSelf: Boolean = true) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createGroup(name)
            _groupId.value = id

            if (includeSelf) {
                userRepository.getUser()?.let { user ->
                    repository.addMember(GroupMemberEntity(groupId = id, name = user.name))
                }
            }
        }
    }

    fun addMember(name: String) {
        val gId = _groupId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addMember(GroupMemberEntity(groupId = gId, name = name))
        }
    }

    fun deleteMember(member: GroupMemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }
}
