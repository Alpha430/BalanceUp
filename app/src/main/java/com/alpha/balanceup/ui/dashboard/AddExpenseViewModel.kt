package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
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

    val groupMembers: Flow<List<GroupMemberEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getMembersForGroup(id) else flowOf(emptyList())
    }

    fun createGroup(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.createGroup(name)
            _groupId.value = id
        }
    }

    fun addMember(name: String) {
        val gId = _groupId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addMember(GroupMemberEntity(groupId = gId, name = name))
        }
    }
}
